// Password Toggle Functionality
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const button = input.parentElement.querySelector('.password-toggle-btn');
    const icon = button.querySelector('i');
    
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fas fa-eye-slash';
        button.classList.add('show-password');
    } else {
        input.type = 'password';
        icon.className = 'fas fa-eye';
        button.classList.remove('show-password');
    }
}

// Add keyboard support for password toggle
document.addEventListener('DOMContentLoaded', function() {
    // Add event listeners for Enter key on password toggle buttons
    const passwordToggleButtons = document.querySelectorAll('.password-toggle-btn');
    
    passwordToggleButtons.forEach(button => {
        button.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                const inputId = this.parentElement.querySelector('input').id;
                togglePassword(inputId);
            }
        });
    });
});
