document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');

    // Si déjà connecté, rediriger
    if (localStorage.getItem('locagest_token')) {
        window.location.href = 'dashboard.html';
    }

    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const password = document.getElementById('password').value;

            // Désactiver le bouton
            const btn = this.querySelector('button');
            btn.disabled = true;
            btn.innerHTML = 'Connexion...';

            // Cacher erreur
            if (errorMessage) errorMessage.style.display = 'none';

            try {
                const response = await fetch('http://localhost:8080/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        email: 'admin@locagest.com',
                        password: password
                    })
                });

                if (response.ok) {
                    const data = await response.json();
                    localStorage.setItem('locagest_token', data.token);
                    window.location.href = 'dashboard.html';
                } else {
                    const error = await response.text();
                    if (errorMessage) {
                        errorMessage.textContent = 'Identifiants incorrects';
                        errorMessage.style.display = 'block';
                    }
                    btn.disabled = false;
                    btn.innerHTML = 'Se connecter <svg class="arrow" width="16" height="16" viewBox="0 0 16 16" fill="#FFFFFF"><path d="M8 0l8 8-8 8-2-2 6-6-6-6z"/></svg>';
                }

            } catch (error) {
                if (errorMessage) {
                    errorMessage.textContent = 'Erreur de connexion';
                    errorMessage.style.display = 'block';
                }
                btn.disabled = false;
                btn.innerHTML = 'Se connecter <svg class="arrow" width="16" height="16" viewBox="0 0 16 16" fill="#FFFFFF"><path d="M8 0l8 8-8 8-2-2 6-6-6-6z"/></svg>';
            }
        });
    }

    // Focus sur le mot de passe
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        passwordInput.focus();
        passwordInput.select();
    }
});