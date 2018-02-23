<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>CAS Login</title>
</head>
<body>
<form action="/login" method="post">
    username: <input name="username">
    password: <input name="password">
    <input hidden name="service" value="${service}">
    <button type="submit">提交</button>
</form>
</body>
</html>