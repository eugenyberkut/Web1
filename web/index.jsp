<%--
  Created by IntelliJ IDEA.
  User: Eugeny
  Date: 10.05.2015
  Time: 20:58
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="css/test.css" />
    <title>Пробное тестирование</title>
  </head>
  <body>
  <h1>Пробное тестирование</h1>
  <div>
    <form action="teststart" method="POST">
      <table>
        <tr>
          <td>Ф.И.О.</td> <td><input type="text" name="fio" value="" required/></td>
        </tr>
        <tr>
          <td>Группа</td> <td> <input type="text" name="group" value="" /></td>
        </tr>
        <tr>
          <td colspan="2"><button class="accept" type="submit" value="Start" >Начать</button></td>
        </tr>
      </table>
    </form>

  </div>
  </body>
</html>
