<%@page import="tje.DTO.HopeBook"%>
<%@page import="tje.Service.HopeBookServiceImpl"%>
<%@page import="tje.Service.HopeBookService"%>
<%@page import="tje.DTO.BookStock"%>
<%@page import="java.util.Date"%>
<%@page import="tje.DTO.RentalList"%>
<%@page import="java.util.List"%>
<%@page import="tje.Service.RentalSerivceImpl"%>
<%@page import="tje.Service.RentalService"%>
<%@page import="tje.DTO.User"%>
<%@page import="tje.Service.UserServiceImpl"%>
<%@page import="tje.Service.UserService"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
//세션 사용 아이디
	User user = (User) session.getAttribute("user");
	UserService userService = new UserServiceImpl();
	
//	user = userService.select("joeun");
	//UserService userService = new UserServiceImpl();
	//User user = userService.select("joeun");
// 	User user = User.builder()
// 					.id("아이디")
// 					.telNumber("010-1234-1234")
// 					.name("조하은")
// 					.build();
	
	// chapter 
	String chapterTitle = "내 서재";
	String chapter1 = "HOME";
	String chapter2 = "내 서재";
	String chapter3 = "대출현황";
	
	RentalService rentalService = new RentalSerivceImpl();
	String state;
	int loanNum=0, rvNum=0, overdueNum=0;
	int overDate=0;
	List<RentalList> userRentalList = rentalService.selectlist(user);
	for(int i=0; i<userRentalList.size(); i++) {
		state = userRentalList.get(i).getState();
		BookStock bookStock = BookStock.builder()
										.stockId(userRentalList.get(i).getStockId())
										.bookId(userRentalList.get(i).getBookId())
										.build();
		if (state.equals("대출")){
			bookStock.setStatus("대출 불가");
			loanNum++;
			overDate = (int)rentalService.overdue(bookStock, user);
			if(overDate>0) overdueNum++;
		} else if (state.equals("예약")){
			rvNum++;
		}
	}
	
	String loanover = loanNum+"("+overdueNum+")";
	
	HopeBookService hopeBookService = new HopeBookServiceImpl();
	List<HopeBook> userHopeList = hopeBookService.selectById(user);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<link rel="stylesheet" href="static/css/mypage.css">
<jsp:include page="/layout/meta.jsp" />
<jsp:include page="/layout/link.jsp" />
</head>
<body>
	<jsp:include page="/layout/header.jsp" />
	<main class="pb-5">
		<div class="pt-5 pb-4">
<%-- 			<%@ include file="/layout/chapter.jsp"%> --%>
		</div>
		<div class="container pb-5">
			<div class="row px-5 py-4 bg-gray">
				<div class="col-12 col-sm-6 py-1">
					<p>
						<b>회원ID :</b>
						<%= user.getId() %></p>
				</div>
				<div class="col-12 col-sm-6 py-1">
					<p>
						<b>전화번호: </b><%= user.getTelNumber() %></p>
				</div>
				<div class="col-12 col-sm-6 py-1">
					<p>
						<b>이름: </b><%= user.getName() %></p>
				</div>
				<div class="col-12 col-sm-6 py-1">
					<p>
					<% if(overDate>0) {%>
						<b>대출 가능 여부: 불가능(연체)</b>
					<%} else { %>
						<b>대출 가능 여부: 가능</b>
					<% }%>
					</p>
				</div>
			</div>
		</div>
		<div class="container">
			<div class=" row justify-content-between fs-3 text-center">
				<div
					class="col-4 row rounded-3 align-item-center p-0 border overflow-hidden">
					<div class="col-6 py-4">대출(연체)</div>
					<div class="col-6 py-4 statuscard"><%= loanover %></div>
				</div>
				<div
					class="col-4 row rounded-3 align-item-center p-0 border overflow-hidden">
					<div class="col-6 py-4">예약</div>
					<div class="col-6 py-4 statuscard"><%= rvNum %></div>
				</div>
				<div
					class="col-4 row rounded-3 align-item-center p-0 border overflow-hidden">
					<div class="col-6 py-4">희망도서</div>
					<div class="col-6 py-4 statuscard"><%=userHopeList.size() %></div>
				</div>
			</div>
		</div>
	</main>
	<jsp:include page="/layout/script.jsp" />
</body>
</html>
