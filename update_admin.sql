USE notif;
UPDATE users SET password = '$2a$10$DaeP.j8wlYNUhP0DL3bj2OH3QaNuJxZwMcGYvOl.Y4k2A0x3YkHHm' WHERE username = 'admin';
SELECT username, LEFT(password, 10) as password_prefix FROM users WHERE username = 'admin';
