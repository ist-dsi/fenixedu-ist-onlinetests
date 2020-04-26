<%--

    Copyright © 2002 Instituto Superior Técnico

    This file is part of FenixEdu Core.

    FenixEdu Core is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu Core is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu Core.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html:xhtml/>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<jsp:include page="/includeMathJax.jsp" />
<logic:present name="studentTestQuestionList">

	<logic:empty name="studentTestQuestionList">
		<h2><bean:message key="message.test.no.available"/></h2>
	</logic:empty>
		
		<logic:notEmpty name="studentTestQuestionList" >
			<bean:message key="message.onlineTest.info" bundle="STUDENT_RESOURCES"/>
			<br/><br/>
			<html:form action="/studentTestManagement">
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.method" property="method" value="simulateTest"/>
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.doTestSimulation" property="doTestSimulation" value="true"/>
			
			<logic:iterate id="testQuestion" name="studentTestQuestionList" type="org.fenixedu.academic.dto.onlineTests.InfoStudentTestQuestion"/>
			<bean:define id="distributedTest" name="testQuestion" property="distributedTest" type="org.fenixedu.academic.dto.onlineTests.InfoDistributedTest"/>
			<bean:define id="distributedTestCode" name="distributedTest" property="externalId"/>
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.testInformation" name="distributedTest" property="testInformation"/>
			<bean:define id="testType" name="distributedTest" property="testType.type"/>
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.testType" property="testType" value="<%=testType.toString()%>"/>
			<bean:define id="availableCorrection" name="distributedTest" property="correctionAvailability.availability"/>
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.availableCorrection" property="availableCorrection" value="<%=availableCorrection.toString()%>"/>
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.imsFeedback" name="distributedTest" property="imsFeedback"/>
			
			<bean:define id="objectCode" name="distributedTest" property="infoTestScope.infoObject.externalId"/>
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.objectCode" property="objectCode" value="<%= objectCode.toString() %>"/>
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.distributedTestCode" property="distributedTestCode" value="<%= distributedTestCode.toString() %>"/>
			<center>
				<h2><bean:write name="distributedTest" property="title"/></h2>
				<b><bean:write name="distributedTest" property="testInformation"/></b>
			</center>
		
		
		    <bean:define id="imsFeedback" name="distributedTest" property="imsFeedback"/>
			<jsp:include page="showStudentTest.jsp">
				<jsp:param name="pageType" value="correction"/>
				<jsp:param name="correctionType" value=""/>
				<jsp:param name="testCode" value="<%=distributedTestCode%>"/>
				<jsp:param name="testType" value="<%=testType%>"/>
				<jsp:param name="correctionAvailability" value="<%=availableCorrection%>"/>
				<jsp:param name="imsFeedback" value="<%=imsFeedback%>"/>
		 	</jsp:include>
			<br/>
			<br/>
			<bean:define id="testType" name="distributedTest" property="testType.type"/>
			
			<br/>
			<br/>
			<table align="center">
			<tr>
				<td><html:submit bundle="HTMLALT_RESOURCES" altKey="submit.submit" styleClass="inputbutton"><bean:message key="label.back"/></html:submit></td>
			</tr>
			</table>
			</html:form>
		</logic:notEmpty>
</logic:present>
<logic:notPresent name="studentTestQuestionList">
<center>
	<h2><bean:message key="message.test.no.available"/></h2>
</center>
</logic:notPresent>
