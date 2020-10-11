<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
</head>
<body>

Auto Script Running  <a href="/automode">click here</a>

manual Script Running  <a href="/manualmodelist">click here</a>
${successorfailure}


<br> <a href="/firstautomodetest">first auto test </a>



${hello}
<form action="/manualmode" method="get">


<table>

<c:forEach items="${fName}" var="fileName"> 
<tr> <td><input type="checkbox" name="fnames" value= "${fileName}"></td><td>
   ${fileName}</td>
</tr>
</c:forEach></table>
calender  <input type="datetime-local"  value="2020-10-08 19:32:00" step="1" name="datetimeloc">
<input type="submit" value="manual search">
</form>

</body>
</html>