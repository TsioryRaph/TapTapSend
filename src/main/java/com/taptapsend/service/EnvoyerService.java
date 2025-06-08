package com.taptapsend.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.Cell;

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
import java.util.Locale;

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

            Client envoyeur = em.find(Client.class, envoi.getEnvoyeur().getNumtel());
            Client recepteur = em.find(Client.class, envoi.getRecepteur().getNumtel());

            if (envoyeur == null) {
                throw new IllegalArgumentException("Expéditeur introuvable: " + envoi.getEnvoyeur().getNumtel());
            }
            if (recepteur == null) {
                throw new IllegalArgumentException("Destinataire introuvable: " + envoi.getRecepteur().getNumtel());
            }

            if (envoyeur.getPays().equals(recepteur.getPays())) {
                throw new IllegalArgumentException("L'envoi doit être vers un pays étranger.");
            }

            Taux taux = tauxDAO.findAll().stream().findFirst().orElseThrow(
                    () -> new IllegalStateException("Aucun taux de change défini."));

            logger.info("Taux de change utilisé: {} EUR = {} MGA", taux.getMontant1(), taux.getMontant2());

            // --- CONVERSION DU MONTANT VERS EUR AVANT CALCUL DES FRAIS ---
            double montantEnEur = envoi.getMontant(); // CHANGÉ: int -> double

            if ("MGA".equals(envoyeur.getDevise())) {
                // Convertir MGA vers EUR : montantMGA * montant1EUR / montant2MGA
                montantEnEur = (envoi.getMontant() * taux.getMontant1()) / taux.getMontant2();
                logger.info("Conversion du montant: {} MGA = {} EUR", envoi.getMontant(), montantEnEur);
            } else {
                logger.info("Montant déjà en EUR: {} EUR", montantEnEur);
            }

            // --- SÉLECTION DES FRAIS BASÉE SUR LE MONTANT EN EUR ---
            List<FraisEnvoi> tousLesFrais = fraisEnvoiDAO.findAll();

            FraisEnvoi frais = null;
            for (FraisEnvoi f : tousLesFrais) {
                logger.debug("Vérification frais: {} EUR <= {} EUR <= {} EUR", f.getMontant1(), montantEnEur, f.getMontant2());
                if (montantEnEur >= f.getMontant1() && montantEnEur <= f.getMontant2()) {
                    frais = f;
                    logger.info("Frais trouvés: ID={}, Montant={} EUR pour {} EUR", f.getIdfrais(), f.getFrais(), montantEnEur);
                    break;
                }
            }

            if (frais == null) {
                throw new IllegalStateException("Aucun frais d'envoi défini pour le montant " + montantEnEur +
                        " EUR (équivalent de " + envoi.getMontant() + " " + envoyeur.getDevise() + "). Veuillez contacter l'administrateur.");
            }

            envoi.setFraisAppliques(frais.getFrais());

            // --- CALCUL DU DÉBIT TOTAL DANS LA DEVISE DE L'ENVOYEUR ---
            double fraisEnDeviseEnvoyeur = frais.getFrais(); // CHANGÉ: int -> double

            if ("MGA".equals(envoyeur.getDevise())) {
                fraisEnDeviseEnvoyeur = (frais.getFrais() * taux.getMontant2()) / taux.getMontant1();
                logger.info("Conversion des frais: {} EUR = {} MGA", frais.getFrais(), fraisEnDeviseEnvoyeur);
            }

            double totalDebit = envoi.getMontant() + fraisEnDeviseEnvoyeur; // CHANGÉ: int -> double
            logger.info("Calcul débit total: {} + {} = {} {}", envoi.getMontant(), fraisEnDeviseEnvoyeur, totalDebit, envoyeur.getDevise());

            if (envoyeur.getSolde() < totalDebit) {
                throw new IllegalArgumentException("Solde insuffisant pour effectuer l'envoi. Solde actuel: " +
                        envoyeur.getSolde() + " " + envoyeur.getDevise() + ", Montant requis: " + totalDebit + " " + envoyeur.getDevise());
            }

            envoyeur.setSolde(envoyeur.getSolde() - totalDebit);
            logger.info("Nouveau solde envoyeur: {} {}", envoyeur.getSolde(), envoyeur.getDevise());

            // --- CALCUL DU MONTANT REÇU PAR LE RÉCEPTEUR ---
            double montantRecu; // CHANGÉ: int -> double
            if ("EUR".equals(envoyeur.getDevise())) {
                montantRecu = (envoi.getMontant() * taux.getMontant2()) / taux.getMontant1();
                logger.info("Conversion pour récepteur: {} EUR = {} MGA", envoi.getMontant(), montantRecu);
            } else {
                montantRecu = (envoi.getMontant() * taux.getMontant1()) / taux.getMontant2();
                logger.info("Conversion pour récepteur: {} MGA = {} EUR", envoi.getMontant(), montantRecu);
            }

            recepteur.setSolde(recepteur.getSolde() + montantRecu);
            logger.info("Nouveau solde récepteur: {} {}", recepteur.getSolde(), recepteur.getDevise());

            em.merge(envoyeur);
            em.merge(recepteur);
            em.persist(envoi);

            et.commit();
            logger.info("Transfert {} effectué avec succès. Envoyeur: {}, Récepteur: {}, Montant: {} {}, Frais: {} EUR",
                    envoi.getIdEnv(), envoyeur.getNumtel(), recepteur.getNumtel(), envoi.getMontant(), envoyeur.getDevise(), frais.getFrais());

            // Emails de notification (adaptés pour double)
            try {
                String messageEnvoyeur = String.format(
                        "Bonjour %s,\n\nVous avez envoyé %.2f %s à %s (%s).\n" +
                                "Les frais d'envoi s'élèvent à %.2f EUR (%.2f %s).\n" +
                                "Votre nouveau solde est de %.2f %s.\n\n" +
                                "Le destinataire recevra %.2f %s.\n\n" +
                                "Merci d'utiliser Taptapsend.",
                        envoyeur.getNom(),
                        envoi.getMontant(), envoyeur.getDevise(),
                        recepteur.getNom(), recepteur.getNumtel(),
                        frais.getFrais(), fraisEnDeviseEnvoyeur, envoyeur.getDevise(),
                        envoyeur.getSolde(), envoyeur.getDevise(),
                        montantRecu, recepteur.getDevise()
                );

                EmailUtil.sendEmail(envoyeur.getMail(), "Confirmation d'envoi d'argent", messageEnvoyeur);

                String messageRecepteur = String.format(
                        "Bonjour %s,\n\nVous avez reçu %.2f %s de %s (%s).\n" +
                                "Votre nouveau solde est de %.2f %s.\n\n" +
                                "Merci d'utiliser Taptapsend.",
                        recepteur.getNom(),
                        montantRecu, recepteur.getDevise(),
                        envoyeur.getNom(), envoyeur.getNumtel(),
                        recepteur.getSolde(), recepteur.getDevise()
                );

                EmailUtil.sendEmail(recepteur.getMail(), "Réception d'argent", messageRecepteur);

                logger.info("Emails envoyés avec succès pour le transfert {}.", envoi.getIdEnv());
            } catch (Exception emailEx) {
                logger.warn("Erreur lors de l'envoi de l'email pour le transfert {} : {}",
                        envoi.getIdEnv(), emailEx.getMessage(), emailEx);
            }

        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback();
            }
            logger.error("Erreur lors de la création de l'envoi : {}", ex.getMessage(), ex);
            throw ex;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
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
            existingEnvoi.setDate(updatedEnvoi.getDate());
            existingEnvoi.setRaison(updatedEnvoi.getRaison());

            em.merge(existingEnvoi);

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
        if (envois == null) {
            return List.of();
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

    /**
     * Récupère la recette totale de l'opérateur.
     *
     * @return Le montant total des frais collectés.
     */
    public double getRecetteTotaleOperateur() {
        double totalRecette = envoyerDAO.getRecetteTotaleOperateur();
        logger.debug("Recette totale de l'opérateur: {}.", totalRecette);
        return totalRecette;
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
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        try {
            // Style commun
            Style normalStyle = new Style()
                    .setFontSize(12)
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA));

            Style boldStyle = new Style()
                    .setFontSize(12)
                    .setBold()
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));

            // -- EN-TÊTE --
            String monthName = java.time.Month.of(month).getDisplayName(
                    java.time.format.TextStyle.FULL, Locale.FRENCH);
            document.add(new Paragraph("Date : " + monthName + " " + year)
                    .addStyle(normalStyle)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            // Informations client - format compact comme dans l'exemple
            Paragraph clientInfo = new Paragraph()
                    .addStyle(normalStyle)
                    .setTextAlignment(TextAlignment.LEFT);

            clientInfo.add("Contact : " + client.getNumtel() + "\n");
            clientInfo.add(client.getNom() + "\n");

            // Ajouter sexe si disponible
            if (client.getSexe() != null && !client.getSexe().isEmpty()) {
                clientInfo.add(client.getSexe() + "\n");
            }

            clientInfo.add("Solde actuel : " + String.format("%.2f", (double)client.getSolde()) + " " + client.getDevise());

            document.add(clientInfo);
            document.add(new Paragraph("\n"));

            // -- TABLEAU DES TRANSACTIONS --
            if (envois != null && !envois.isEmpty()) {
                // Création du tableau avec 4 colonnes
                float[] columnWidths = {2f, 3f, 3f, 2f};
                Table table = new Table(columnWidths);
                table.setWidth(UnitValue.createPercentValue(100));

                // En-têtes du tableau
                table.addHeaderCell(createCell("Date", true, TextAlignment.CENTER));
                table.addHeaderCell(createCell("Raison", true, TextAlignment.CENTER));
                table.addHeaderCell(createCell("Nom du récepteur", true, TextAlignment.CENTER));
                table.addHeaderCell(createCell("Montant", true, TextAlignment.CENTER));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                double totalDebit = 0.0;

                // Remplissage des données
                for (Envoyer envoi : envois) {
                    String recepteurNom = (envoi.getRecepteur() != null) ? envoi.getRecepteur().getNom() : "Inconnu";
                    String dateFormatted = (envoi.getDate() != null) ? envoi.getDate().format(formatter) : "N/A";

                    table.addCell(createCell(dateFormatted, false, TextAlignment.LEFT));
                    table.addCell(createCell(envoi.getRaison(), false, TextAlignment.LEFT));
                    table.addCell(createCell(recepteurNom, false, TextAlignment.LEFT));
                    table.addCell(createCell(String.format("%.2f", (double)envoi.getMontant()), false, TextAlignment.RIGHT));

                    totalDebit += envoi.getMontant();
                }

                document.add(table);
                document.add(new Paragraph("\n"));

                // Total débit
                document.add(new Paragraph("Total Débit : " + String.format("%.2f", totalDebit) + " " + client.getDevise())
                        .addStyle(boldStyle)
                        .setTextAlignment(TextAlignment.RIGHT));
            } else {
                document.add(new Paragraph("Aucun envoi trouvé pour cette période.")
                        .addStyle(normalStyle));
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

    // Méthode utilitaire pour créer une cellule
    private Cell createCell(String text, boolean isHeader, TextAlignment alignment) {
        Paragraph p = new Paragraph(text);
        if (isHeader) {
            p.setBold();
        }

        Cell cell = new Cell().add(p);
        cell.setTextAlignment(alignment);
        cell.setPadding(5);
        cell.setBorder(new SolidBorder(0.5f));

        return cell;
    }
}