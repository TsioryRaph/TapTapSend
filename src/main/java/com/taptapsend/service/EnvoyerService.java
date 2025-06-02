package com.taptapsend.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
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
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EnvoyerService {
    // Les DAOs sont des dépendances. Idéalement, ils devraient être injectés.
    // Pour cet exemple, nous les instancions directement.
    private final EnvoyerDAO envoyerDAO;
    private final ClientDAO clientDAO;
    private final TauxDAO tauxDAO;
    private final FraisEnvoiDAO fraisEnvoiDAO;

    public EnvoyerService() {
        // Initialisation des DAOs. Ils doivent être prêts à recevoir un EntityManager.
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
    public void createEnvoi(Envoyer envoi) throws Exception { // Déclare explicitement Exception pour le relancer
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction et = null;

        try {
            et = em.getTransaction();
            et.begin();

            // S'assurer que les DAOs utilisent l'EntityManager de cette transaction
            // Pour cela, vos DAOs doivent avoir des méthodes qui acceptent un EntityManager
            // ou être instanciés à l'intérieur de la transaction avec cet EM.
            // Si vos DAOs créent leur propre EM, cela ne fonctionnera pas comme une transaction unique.
            // L'approche la plus simple est de passer l'EM aux méthodes DAO qui en ont besoin.

            // Récupérer les entités Client dans le contexte de l'EntityManager actuel
            // en utilisant `em.find` est crucial ici car elles sont modifiées.
            Client envoyeur = em.find(Client.class, envoi.getEnvoyeur().getNumtel());
            Client recepteur = em.find(Client.class, envoi.getRecepteur().getNumtel());

            if (envoyeur == null || recepteur == null) {
                throw new IllegalArgumentException("Envoyeur ou récepteur introuvable.");
            }

            // Vérifier que l'envoi est vers un pays étranger
            if (envoyeur.getPays().equals(recepteur.getPays())) {
                throw new IllegalArgumentException("L'envoi doit être vers un pays étranger.");
            }

            // Trouver le taux de change et les frais d'envoi.
            // Ces opérations de lecture n'ont pas besoin d'être dans la même transaction si elles ne sont pas modifiées.
            // Cependant, pour l'exemple, nous les gardons pour la cohérence.
            // Idéalement, ces appels DAO devraient aussi prendre l'EntityManager si le DAO utilise une transaction.
            Taux taux = tauxDAO.findAll().stream().findFirst().orElseThrow(
                    () -> new IllegalStateException("Aucun taux de change défini."));

            FraisEnvoi frais = fraisEnvoiDAO.findAll().stream().findFirst().orElseThrow(
                    () -> new IllegalStateException("Aucun frais d'envoi défini."));

            // Calculer le total à débiter (montant + frais)
            int totalDebit = envoi.getMontant() + frais.getFrais();

            // Vérifier le solde de l'envoyeur
            if (envoyeur.getSolde() < totalDebit) {
                throw new IllegalArgumentException("Solde insuffisant pour effectuer l'envoi.");
            }

            // Mettre à jour les soldes des entités gérées par l'EntityManager
            envoyeur.setSolde(envoyeur.getSolde() - totalDebit);
            int montantRecu = (envoi.getMontant() * taux.getMontant2()) / taux.getMontant1();
            recepteur.setSolde(recepteur.getSolde() + montantRecu);

            // em.merge() sur des entités déjà gérées n'est pas strictement nécessaire
            // car les changements sont suivis par l'EM, mais ne fait pas de mal.
            // Cela serait nécessaire si `envoyeur` ou `recepteur` venaient d'un contexte persistant différent.
            em.merge(envoyeur); // Synchronise les changements sur l'envoyeur
            em.merge(recepteur); // Synchronise les changements sur le recepteur

            // Enregistrer la nouvelle entité Envoyer
            envoi.setDate(LocalDateTime.now());
            em.persist(envoi); // Persiste la nouvelle entité envoi

            et.commit(); // Valide toutes les opérations (client, recepteur, envoi)

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
                                " EUR de " + envoyeur.getNom() + " (" + envoyeur.getNumtel() + ")" +
                                ".\nVotre nouveau solde est de " + recepteur.getSolde() + " EUR." +
                                "\n\nMerci d'utiliser Taptapsend.");
            } catch (Exception emailEx) {
                System.err.println("Erreur lors de l'envoi de l'email (l'opération a été enregistrée) : " + emailEx.getMessage());
                // Ne pas relancer, car l'envoi a déjà été enregistré.
            }

        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback(); // Annule la transaction en cas d'erreur
            }
            ex.printStackTrace(); // Log l'exception pour le débogage
            throw ex; // Relance l'exception pour que le contrôleur puisse la gérer
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
        return envoyerDAO.findById(id); // Assurez-vous que votre DAO a une méthode findById
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

            // Récupérer l'entité existante si nécessaire pour la gestion du cycle de vie
            Envoyer existingEnvoi = em.find(Envoyer.class, updatedEnvoi.getIdEnv());
            if (existingEnvoi == null) {
                throw new IllegalArgumentException("Envoi introuvable pour la mise à jour avec ID: " + updatedEnvoi.getIdEnv());
            }

            // Mettre à jour les propriétés de l'entité gérée avec les nouvelles valeurs
            // ATTENTION : Pour les champs @ManyToOne comme envoyeur et recepteur,
            // vous devez soit les recharger dans le même EM, soit utiliser em.merge sur le updatedEnvoi
            // si les objets Client ne sont pas des entités gérées.
            // Pour l'instant, je vais supposer que les objets Client dans updatedEnvoi sont complets et peuvent être mergés.
            existingEnvoi.setEnvoyeur(em.find(Client.class, updatedEnvoi.getEnvoyeur().getNumtel()));
            existingEnvoi.setRecepteur(em.find(Client.class, updatedEnvoi.getRecepteur().getNumtel()));
            existingEnvoi.setMontant(updatedEnvoi.getMontant());
            existingEnvoi.setDate(updatedEnvoi.getDate()); // Ou LocalDateTime.now() si la date de modification est importante
            existingEnvoi.setRaison(updatedEnvoi.getRaison());

            // Si vous n'avez pas rechargé les clients, vous pouvez faire un simple merge
            // em.merge(updatedEnvoi);

            et.commit();
        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback();
            }
            ex.printStackTrace();
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
            } else {
                throw new IllegalArgumentException("Envoi introuvable avec ID: " + id);
            }

            et.commit();
        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback();
            }
            ex.printStackTrace();
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
        return envoyerDAO.findAll();
    }

    public List<Envoyer> getEnvoisByDate(LocalDateTime date) {
        return envoyerDAO.findByDate(date);
    }

    public List<Envoyer> getEnvoisByEnvoyeurAndMonth(String numtel, int month, int year) {
        return envoyerDAO.findByEnvoyeurAndMonth(numtel, month, year);
    }

    public double getTotalFrais() {
        return envoyerDAO.getTotalFrais();
    }

    // ... (Votre méthode generatePdfReleve est déjà correcte pour la gestion des exceptions et le formatage) ...
    public byte[] generatePdfReleve(Client client, List<Envoyer> envois, int month, int year)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            document.add(new Paragraph("Relevé des envois"));
            document.add(new Paragraph("Client: " + client.getNom() + " (" + client.getNumtel() + ")"));
            document.add(new Paragraph("Période: " + String.format("%02d", month) + "/" + year));
            document.add(new Paragraph("\n"));

            if (envois != null && !envois.isEmpty()) {
                document.add(new Paragraph("Détails des transactions:"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                for (Envoyer envoi : envois) {
                    String recepteurInfo = (envoi.getRecepteur() != null) ?
                            envoi.getRecepteur().getNom() + " (" + envoi.getRecepteur().getNumtel() + ")" :
                            "Inconnu";
                    String dateFormatted = (envoi.getDate() != null) ? envoi.getDate().format(formatter) : "N/A";

                    document.add(new Paragraph("ID: " + envoi.getIdEnv() +
                            ", Montant: " + envoi.getMontant() + " EUR" +
                            ", Récepteur: " + recepteurInfo +
                            ", Date: " + dateFormatted +
                            ", Raison: " + envoi.getRaison()));
                }
            } else {
                document.add(new Paragraph("Aucun envoi trouvé pour cette période."));
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Erreur lors de la génération du PDF.", e);
        } finally {
            if (document != null) {
                document.close();
            }
        }
        return baos.toByteArray();
    }
}