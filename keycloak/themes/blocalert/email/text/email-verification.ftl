Hi ${user.firstName!user.username},

Welcome to BlocAlert! Please verify your email address by clicking the link below:

${link}

This link expires in ${linkExpirationFormatter(linkExpiration)}.

If you did not request this verification, you can safely ignore this email.

© ${.now?string("yyyy")} BlocAlert