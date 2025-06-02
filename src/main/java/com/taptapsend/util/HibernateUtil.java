package com.taptapsend.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateUtil {
    private static final EntityManagerFactory entityManagerFactory;

    static {
        try {
            // Remplacez "taptapsend-pu" par le nom de votre unité de persistance (défini dans persistence.xml)
            entityManagerFactory = Persistence.createEntityManagerFactory("taptapsend-pu");
        } catch (Throwable ex) {
            System.err.println("Échec de la création d'EntityManagerFactory : " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
}