Hi ${user.firstName!user.username},

We received a request to reset your password. Click the link below to create a new password:

${link}

This link will expire in ${linkExpirationFormatter(linkExpiration)}.

If you didn't request a password reset, you can safely ignore this email.

© ${.now?string("yyyy")} BlocAlert