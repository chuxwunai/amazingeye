<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="UTF-8"%>
<%
   String aa =  request.getProtocol()+"://"+request.getScheme()+" "+request.getServerName()+request.getServerPort();
System.out.println(aa);
%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />
<meta name="format-detection" content="telephone=no">
<meta charset="UTF-8">
<title>首页</title>
</head>
<body>
	<template w-app> Hello W.js! </template>


<script src="js/w.js"></script>
</body>
</html>