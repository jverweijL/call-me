<%@ include file="/init.jsp" %>

<portlet:actionURL var="callmeURL" name="/callme"></portlet:actionURL>

<aui:form action="<%= callmeURL %>" method="post" name="fm">
    <clay:button label="Call me" type="submit"/>
</aui:form>