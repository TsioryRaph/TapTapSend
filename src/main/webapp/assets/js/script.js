// Attendre que le DOM soit chargé
document.addEventListener('DOMContentLoaded', function() {
    console.log('Document prêt !');

    // Exemple : Gestion des clics sur les cartes
    const cards = document.querySelectorAll('.card');
    cards.forEach(card => {
        card.addEventListener('click', function() {
            console.log('Carte cliquée : ', this.innerText);
            // Ajoutez ici des interactions dynamiques
        });
    });

    // Exemple : Validation de formulaire (si vous en avez un)
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            console.log('Formulaire soumis !');
            // Ajoutez ici la logique de validation
        });
    }
});

// Fonction utilitaire pour les requêtes AJAX (si nécessaire)
function fetchData(url, callback) {
    fetch(url)
        .then(response => response.json())
        .then(data => callback(data))
        .catch(error => console.error('Erreur : ', error));
}