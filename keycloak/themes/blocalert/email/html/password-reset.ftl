<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Your Password - BlocAlert</title>
  </head>
  <body style="font-family: Arial, sans-serif; background-color: #f6f9fc; margin: 0; padding: 0;">
    <table width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color: #f6f9fc;">
      <tr>
        <td align="center" style="padding: 30px 0;">
          <table width="600" cellspacing="0" cellpadding="0" border="0" style="max-width: 600px; width: 100%; background-color: #ffffff; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden;">
            <tr>
              <td align="center" style="background: linear-gradient(90deg, #0040ff, #007bff); color: white; padding: 24px 0;">
                <h1 style="margin: 0; font-size: 24px; font-weight: bold; letter-spacing: 0.5px;">BlocAlert</h1>
              </td>
            </tr>
            <tr>
              <td style="padding: 40px 30px;">
                <p style="font-size: 16px; color: #333; margin: 0 0 20px 0;">
                  Hi ${user.firstName!user.username},
                </p>
                <h2 style="margin: 0 0 16px 0; font-size: 24px; color: #333; font-weight: 600; text-align: center;">
                  Reset Your Password
                </h2>
                <p style="font-size: 16px; color: #555; line-height: 1.6; margin: 0 0 30px 0; text-align: center;">
                  We received a request to reset your password. Click the button below to create a new password.
                </p>
                <table width="100%" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                    <td align="center" style="padding: 10px 0 30px 0;">
                      <a href="${link}" style="display: inline-block; background-color: #007bff; color: #ffffff; padding: 14px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 16px;">
                        Reset Password
                      </a>
                    </td>
                  </tr>
                </table>
                <p style="font-size: 14px; color: #666; line-height: 1.5; margin: 0 0 20px 0; text-align: center;">
                  This link will expire in <strong>${linkExpirationFormatter(linkExpiration)}</strong>.
                </p>
                <p style="font-size: 14px; color: #666; line-height: 1.5; margin: 0; text-align: center;">
                  If you didn't request a password reset, you can safely ignore this email.
                </p>
              </td>
            </tr>
            <tr>
              <td align="center" style="color: #888; font-size: 13px; padding: 24px 30px; border-top: 1px solid #eee; background-color: #f9f9f9;">
                <p style="margin: 0 0 8px 0;">You're receiving this because a password reset was requested for your account.</p>
                <p style="margin: 0; color: #aaa;">&copy; ${.now?string("yyyy")} BlocAlert. All rights reserved.</p>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </body>
</html>