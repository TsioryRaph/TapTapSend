package com.taptapsend.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table; // Importez la classe Table
import com.itextpdf.layout.properties.TextAlignment; // Pour l'alignement du texte dans les cellules
import com.itextpdf.layout.properties.UnitValue; // Pour définir la largeur des colonnes
import com.itextpdf.layout.element.Cell; // Pour les cellules du tableau

import java.io.IOException;

import com.taptapsend.dao.ClientDAO;
import com.taptapsend.dao.EnvoyerDAO;
import com.taptapsend.dao.FraisEnvoiDAO;
import com.taptapsend.dao.TauxDAO;
import com.taptapsend.model.Client;
import com.taptapsend.model.Envoyer;
import com.taptapsend.model.FraisEnvoi;
import com.taptapsend.model.Taux;
import com.taptapsend.util.EmailUtil;
import com.taptapsend.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale; // Pour le nom du mois en français

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvoyerService {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyerService.class);

    private final EnvoyerDAO envoyerDAO;
    private final ClientDAO clientDAO;
    private final TauxDAO tauxDAO;
    private final FraisEnvoiDAO fraisEnvoiDAO;

    public EnvoyerService() {
        this.envoyerDAO = new EnvoyerDAO();
        this.clientDAO = new ClientDAO();
        this.tauxDAO = new TauxDAO();
        this.fraisEnvoiDAO = new FraisEnvoiDAO();
    }

    /**
     * Crée une nouvelle opération d'envoi, gère les soldes des clients,
     * les frais, le taux de change et envoie des notifications par email.
     * Cette méthode gère sa propre transaction EntityManager pour garantir l'atomicité.
     *
     * @param envoi L'objet Envoyer à créer.
     * @throws IllegalArgumentException Si l'envoyeur/récepteur est introuvable,
     * si l'envoi est vers le même pays,
     * ou si le solde de l'envoyeur est insuffisant.
     * @throws IllegalStateException    Si aucun taux de change ou frais d'envoi n'est défini.
     * @throws Exception                Pour toute autre erreur lors de la transaction ou de l'envoi d'email.
     */
    public void createEnvoi(Envoyer envoi) throws Exception {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction et = null;

        try {
            et = em.getTransaction();
            et.begin();

            // Récupérer les entités Client dans le contexte de l'EntityManager actuel
            Client envoyeur = em.find(Client.class, envoi.getEnvoyeur().getNumtel());
            Client recepteur = em.find(Client.class, envoi.getRecepteur().getNumtel());

            if (envoyeur == null) {
                throw new IllegalArgumentException("Expéditeur introuvable: " + envoi.getEnvoyeur().getNumtel());
            }
            if (recepteur == null) {
                throw new IllegalArgumentException("Destinataire introuvable: " + envoi.getRecepteur().getNumtel());
            }

            // Vérifier que l'envoi est vers un pays étranger
            if (envoyeur.getPays().equals(recepteur.getPays())) {
                throw new IllegalArgumentException("L'envoi doit être vers un pays étranger.");
            }

            // Trouver le taux de change et les frais d'envoi.
            // IMPORTANT : Pour que ces DAOs opèrent dans la même transaction,
            // idéalement les DAOs devraient avoir des méthodes qui acceptent un EntityManager
            // et ne pas créer/fermer leur propre EM à chaque appel.
            // Pour l'instant, je vais laisser votre structure actuelle qui est "un peu" transactionnelle
            // mais ce n'est pas le pattern le plus robuste. Les findAll() ouvrent et ferment leur propre EM.
            Taux taux = tauxDAO.findAll().stream().findFirst().orElseThrow(
                    () -> new IllegalStateException("Aucun taux de change défini."));

            FraisEnvoi frais = fraisEnvoiDAO.findAll().stream().findFirst().orElseThrow(
                    () -> new IllegalStateException("Aucun frais d'envoi défini."));

            int totalDebit = envoi.getMontant() + frais.getFrais();

            if (envoyeur.getSolde() < totalDebit) {
                throw new IllegalArgumentException("Solde insuffisant pour effectuer l'envoi. Solde actuel: " + envoyeur.getSolde() + ", Montant requis: " + totalDebit);
            }

            envoyeur.setSolde(envoyeur.getSolde() - totalDebit);
            // Assurez-vous que le calcul du montant reçu est correct (int / int peut tronquer)
            // Si montant1 ou montant2 peuvent être des doubles, utilisez des doubles pour le calcul.
            // Pour l'instant, je suppose qu'ils sont des entiers et que vous voulez une division entière.
            int montantRecu = (envoi.getMontant() * taux.getMontant2()) / taux.getMontant1();
            recepteur.setSolde(recepteur.getSolde() + montantRecu);

            em.merge(envoyeur); // Met à jour l'envoyeur dans la base de données
            em.merge(recepteur); // Met à jour le récepteur dans la base de données

            envoi.setDate(LocalDateTime.now()); // Définit la date de l'envoi à l'heure actuelle
            em.persist(envoi); // Persiste la nouvelle entité envoi

            et.commit(); // Valide la transaction
            logger.info("Transfert {} effectué avec succès. Envoyeur: {}, Récepteur: {}", envoi.getIdEnv(), envoyeur.getNumtel(), recepteur.getNumtel());

            // Envoyer les emails de notification APRÈS le commit pour s'assurer que la transaction a réussi
            try {
                EmailUtil.sendEmail(
                        envoyeur.getMail(),
                        "Confirmation d'envoi d'argent",
                        "Bonjour " + envoyeur.getNom() + ",\n\nVous avez envoyé " + envoi.getMontant() +
                                " EUR à " + recepteur.getNom() + " (" + recepteur.getNumtel() + ")" +
                                ". Les frais d'envoi s'élèvent à " + frais.getFrais() + " EUR." +
                                "\nVotre nouveau solde est de " + envoyeur.getSolde() + " EUR." +
                                "\n\nMerci d'utiliser Taptapsend.");

                EmailUtil.sendEmail(
                        recepteur.getMail(),
                        "Réception d'argent",
                        "Bonjour " + recepteur.getNom() + ",\n\nVous avez reçu " + montantRecu +
                                " MGA de " + envoyeur.getNom() + " (" + envoyeur.getNumtel() + ")" +
                                ".\nVotre nouveau solde est de " + recepteur.getSolde() + " MGA." +
                                "\n\nMerci d'utiliser Taptapsend.");
                logger.info("Emails envoyés avec succès pour le transfert {}.", envoi.getIdEnv());
            } catch (Exception emailEx) {
                logger.warn("Erreur lors de l'envoi de l'email (l'opération a été enregistrée) pour le transfert {}) : {}", envoi.getIdEnv(), emailEx.getMessage(), emailEx);
            }

        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback(); // Annule la transaction en cas d'erreur
            }
            logger.error("Erreur lors de la création de l'envoi : {}", ex.getMessage(), ex);
            throw ex; // Re-lancer l'exception pour que le contrôleur puisse la gérer
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Ferme l'EntityManager
            }
        }
    }

    /**
     * Récupère une opération d'envoi par son ID.
     * @param id L'ID de l'envoi.
     * @return L'objet Envoyer correspondant, ou null si non trouvé.
     */
    public Envoyer getEnvoiById(String id) {
        Envoyer envoi = envoyerDAO.findById(id);
        if (envoi == null) {
            logger.info("Envoi avec ID {} non trouvé.", id);
        } else {
            logger.debug("Envoi avec ID {} trouvé.", id);
        }
        return envoi;
    }

    /**
     * Met à jour une opération d'envoi existante.
     * Cette méthode doit gérer sa propre transaction si elle est appelée indépendamment.
     *
     * @param updatedEnvoi L'objet Envoyer avec les données mises à jour.
     * @throws Exception En cas d'erreur lors de la mise à jour.
     */
    public void updateEnvoi(Envoyer updatedEnvoi) throws Exception {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction et = null;
        try {
            et = em.getTransaction();
            et.begin();

            Envoyer existingEnvoi = em.find(Envoyer.class, updatedEnvoi.getIdEnv());
            if (existingEnvoi == null) {
                throw new IllegalArgumentException("Envoi introuvable pour la mise à jour avec ID: " + updatedEnvoi.getIdEnv());
            }

            // Recharge les clients dans le contexte de l'EM actuel pour éviter les problèmes de gestion d'entités
            Client envoyeur = em.find(Client.class, updatedEnvoi.getEnvoyeur().getNumtel());
            Client recepteur = em.find(Client.class, updatedEnvoi.getRecepteur().getNumtel());

            if (envoyeur == null || recepteur == null) {
                throw new IllegalArgumentException("Expéditeur ou destinataire introuvable lors de la mise à jour.");
            }
            if (envoyeur.getNumtel().equals(recepteur.getNumtel())) {
                throw new IllegalArgumentException("L'expéditeur et le destinataire ne peuvent pas être les mêmes lors de la mise à jour.");
            }

            // Mettre à jour les propriétés de l'entité gérée avec les nouvelles valeurs
            existingEnvoi.setEnvoyeur(envoyeur);
            existingEnvoi.setRecepteur(recepteur);
            existingEnvoi.setMontant(updatedEnvoi.getMontant());
            // Conserver la date originale ou mettre à jour avec LocalDateTime.now() si c'est une date de modification
            existingEnvoi.setDate(updatedEnvoi.getDate()); // Ou LocalDateTime.now() si c'est une date de modification
            existingEnvoi.setRaison(updatedEnvoi.getRaison());

            em.merge(existingEnvoi); // Merge explicite pour s'assurer que les changements sont pris en compte

            et.commit();
            logger.info("Envoi avec ID {} mis à jour avec succès.", updatedEnvoi.getIdEnv());

        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback();
            }
            logger.error("Erreur lors de la mise à jour de l'envoi {}: {}", updatedEnvoi.getIdEnv(), ex.getMessage(), ex);
            throw ex;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }


    /**
     * Supprime une opération d'envoi par son ID.
     * Cette méthode doit gérer sa propre transaction si elle est appelée indépendamment.
     *
     * @param id L'ID de l'envoi à supprimer.
     * @throws Exception En cas d'erreur lors de la suppression.
     */
    public void deleteEnvoi(String id) throws Exception {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction et = null;
        try {
            et = em.getTransaction();
            et.begin();

            Envoyer envoi = em.find(Envoyer.class, id);
            if (envoi != null) {
                em.remove(envoi);
                logger.info("Envoi avec ID {} supprimé avec succès.", id);
            } else {
                throw new IllegalArgumentException("Envoi introuvable avec ID: " + id);
            }

            et.commit();
        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback();
            }
            logger.error("Erreur lors de la suppression de l'envoi {}: {}", id, ex.getMessage(), ex);
            throw ex;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }


    /**
     * Récupère toutes les opérations d'envoi.
     * @return La liste de toutes les opérations d'envoi.
     */
    public List<Envoyer> getAllEnvois() {
        List<Envoyer> envois = envoyerDAO.findAll();
        if (envois == null) { // Assurez-vous que le DAO retourne une liste vide au lieu de null
            return List.of(); // Utilisation de List.of() (Java 9+) pour retourner une liste immuable vide
        }
        logger.debug("Récupération de {} envois.", envois.size());
        return envois;
    }

    public List<Envoyer> getEnvoisByDate(LocalDateTime date) {
        List<Envoyer> envois = envoyerDAO.findByDate(date);
        if (envois == null) {
            return List.of();
        }
        logger.debug("Récupération de {} envois pour la date {}.", envois.size(), date.toLocalDate());
        return envois;
    }

    public List<Envoyer> getEnvoisByEnvoyeurAndMonth(String numtel, int month, int year) {
        List<Envoyer> envois = envoyerDAO.findByEnvoyeurAndMonth(numtel, month, year);
        if (envois == null) {
            return List.of();
        }
        logger.debug("Récupération de {} envois pour l'envoyeur {} en {}/{}", envois.size(), numtel, month, year);
        return envois;
    }

    public double getTotalFrais() {
        double total = envoyerDAO.getTotalFrais();
        logger.debug("Total des frais: {}.", total);
        return total;
    }

    /**
     * Génère un relevé PDF des envois pour un client donné sur une période spécifique.
     * Le format du PDF est calqué sur l'exemple fourni dans le sujet.
     *
     * @param client Le client pour lequel générer le relevé.
     * @param envois La liste des envois à inclure dans le relevé.
     * @param month Le mois du relevé.
     * @param year L'année du relevé.
     * @return Un tableau de bytes représentant le contenu du PDF.
     * @throws IOException En cas d'erreur lors de la génération du PDF.
     */
    public byte[] generatePdfReleve(Client client, List<Envoyer> envois, int month, int year)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // -- EN-TÊTE DU RELEVÉ --
            // Date de la période
            String monthName = java.time.Month.of(month).getDisplayName(
                    java.time.format.TextStyle.FULL, Locale.FRENCH); // Utiliser Locale.FRENCH
            document.add(new Paragraph("Date : " + monthName + " " + year).setTextAlignment(TextAlignment.LEFT));

            // Informations du client
            document.add(new Paragraph("Contact : " + client.getNumtel()).setTextAlignment(TextAlignment.LEFT));
            document.add(new Paragraph(client.getNom()).setTextAlignment(TextAlignment.LEFT));

            // Lignes supprimées pour éviter l'erreur "Cannot resolve method 'getAge' in 'Client'"
            // et "Cannot resolve method 'getSexe' in 'Client'"
            /*
            if (client.getAge() != 0) {
                document.add(new Paragraph(client.getAge() + " ans").setTextAlignment(TextAlignment.LEFT));
            }
            if (client.getSexe() != null && !client.getSexe().isEmpty()) {
                document.add(new Paragraph(client.getSexe()).setTextAlignment(TextAlignment.LEFT));
            }
            */

            // Solde actuel
            document.add(new Paragraph("Solde actuel : " + String.format("%.2f", (double)client.getSolde()) + " EUR").setTextAlignment(TextAlignment.LEFT));
            document.add(new Paragraph("\n")); // Ligne vide pour l'espacement

            // -- DÉTAILS DES TRANSACTIONS (TABLEAU) --
            document.add(new Paragraph("Détails des transactions:").setBold().setTextAlignment(TextAlignment.LEFT));
            document.add(new Paragraph("\n"));

            if (envois != null && !envois.isEmpty()) {
                // Créer un tableau avec 4 colonnes (Date, Raison, Nom du récepteur, Montant)
                // Ajustez les largeurs relatives selon le contenu attendu pour chaque colonne
                float[] columnWidths = {2f, 2f, 3f, 1.5f}; // Exemple: Date, Raison, Nom du récepteur, Montant
                // CORRECTION ICI: Utiliser directement le tableau de floats pour le constructeur de Table
                Table table = new Table(columnWidths);
                table.setWidth(UnitValue.createPercentValue(100)); // Le tableau prend 100% de la largeur disponible

                // Ajouter les en-têtes du tableau
                table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()).setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Cell().add(new Paragraph("Raison").setBold()).setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Cell().add(new Paragraph("Nom du récepteur").setBold()).setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Cell().add(new Paragraph("Montant").setBold()).setTextAlignment(TextAlignment.RIGHT));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Format Date seulement pour le tableau
                double totalDebit = 0.0; // Variable pour accumuler le total des montants envoyés

                for (Envoyer envoi : envois) {
                    String recepteurNom = (envoi.getRecepteur() != null) ? envoi.getRecepteur().getNom() : "Inconnu";
                    String dateFormatted = (envoi.getDate() != null) ? envoi.getDate().format(formatter) : "N/A";

                    table.addCell(new Cell().add(new Paragraph(dateFormatted)).setTextAlignment(TextAlignment.CENTER));
                    table.addCell(new Cell().add(new Paragraph(envoi.getRaison())).setTextAlignment(TextAlignment.LEFT));
                    table.addCell(new Cell().add(new Paragraph(recepteurNom)).setTextAlignment(TextAlignment.LEFT));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f EUR", (double)envoi.getMontant()))).setTextAlignment(TextAlignment.RIGHT));

                    totalDebit += envoi.getMontant(); // Accumuler le montant envoyé
                }

                document.add(table);

                // -- TOTAL DÉBIT --
                document.add(new Paragraph("\n")); // Ligne vide pour l'espacement
                document.add(new Paragraph("Total Débit : " + String.format("%.2f EUR", totalDebit)).setBold().setTextAlignment(TextAlignment.RIGHT));

            } else {
                document.add(new Paragraph("Aucun envoi trouvé pour cette période."));
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du PDF: {}", e.getMessage(), e);
            throw new IOException("Erreur lors de la génération du PDF.", e);
        } finally {
            if (document != null) {
                document.close();
            }
        }
        return baos.toByteArray();
    }
}