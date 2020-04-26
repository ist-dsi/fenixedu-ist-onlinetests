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
<logic:present name="infoSiteStudentTestFeedback">
	<center><h2><bean:message key="message.studentTest.sent"/></h2></center>
	<bean:define id="responseNumber" name="infoSiteStudentTestFeedback" property="responseNumber"/>
	<bean:define id="notResponseNumber" name="infoSiteStudentTestFeedback" property="notResponseNumber"/>
	<bean:define id="errors" name="infoSiteStudentTestFeedback" property="errors"/>
	<table>
		<logic:iterate id="error" name="errors">
			<tr><td><span class="error"><!-- Error messages go here --><bean:write name="error"/></span></td></tr>
		</logic:iterate>
		<tr>
			<td><b><bean:message key="message.studentQuestionsAnsweredNumber"/></b></td>
			<td><bean:write name="responseNumber"/></td>
		</tr>
		<tr>
			<td><b><bean:message key="message.studentQuestionsNotAnsweredNumber"/></b></td>
			<td><bean:write name="notResponseNumber"/></td>
		</tr>
	</table>
	
	
	
	<html:form action="/studentTestManagement">
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.method" property="method" value="simulateTest"/>
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.doTestSimulation" property="doTestSimulation" value="true"/>
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.objectCode" property="objectCode" value="<%= request.getParameter("objectCode") %>"/>
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.distributedTestCode" property="distributedTestCode" value="<%= request.getParameter("distributedTestCode") %>"/>


	<bean:define id="studentTestQuestionList" name="infoSiteStudentTestFeedback" property="studentTestQuestionList"/>
	<logic:iterate id="testQuestion" name="studentTestQuestionList" type="org.fenixedu.academic.dto.onlineTests.InfoStudentTestQuestion"/>
	<bean:define id="distributedTest" name="testQuestion" property="distributedTest" type="org.fenixedu.academic.dto.onlineTests.InfoDistributedTest"/>
	<bean:define id="distributedTestCode" name="distributedTest" property="externalId"/>
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.testInformation" name="distributedTest" property="testInformation"/>
	<bean:define id="testType" name="distributedTest" property="testType.type"/>
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.testType" property="testType" value="<%=testType.toString()%>"/>
	<bean:define id="availableCorrection" name="distributedTest" property="correctionAvailability.availability"/>
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.availableCorrection" property="availableCorrection" value="<%=availableCorrection.toString()%>"/>
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.imsFeedback" name="distributedTest" property="imsFeedback"/>
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.distributedTestCode" property="distributedTestCode" value="<%=distributedTestCode.toString()%>"/>
	<center>
	<h2><bean:write name="distributedTest" property="title"/></h2>
	<b><bean:write name="distributedTest" property="testInformation"/></b>
	</center>
	<br/>
	<br/>
	<bean:define id="imsFeedback" name="distributedTest" property="imsFeedback"/>
	<bean:define id="correctionAvailability" name="availableCorrection"/>
	<logic:equal name="correctionAvailability" value="3">
		<bean:define id="correctionAvailability" value="<%=String.valueOf(org.fenixedu.academic.util.tests.CorrectionAvailability.NEVER)%>"/>
	</logic:equal>
	<jsp:include page="showStudentTest.jsp">
		<jsp:param name="pageType" value="feedback"/>
		<jsp:param name="correctionType" value=""/>
		<jsp:param name="testCode" value="<%=distributedTestCode%>"/>
		<jsp:param name="testType" value="<%=testType%>"/>
		<jsp:param name="correctionAvailability" value="<%=correctionAvailability%>"/>
		<jsp:param name="imsFeedback" value="<%=imsFeedback%>"/>
 	</jsp:include>
	<br/>
	<br/>
	<tr>
	<center>
	<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.showCorrection" property="showCorrection" value="true"/>
	<html:submit bundle="HTMLALT_RESOURCES" altKey="submit.submit" styleClass="inputbutton"><bean:message key="button.back"/></html:submit>
	</center>
	</html:form>
</logic:present>
<logic:notPresent name="infoSiteStudentTestFeedback">
	<center><h2><bean:message key="message.studentTest.notSent"/></h2></center>
</logic:notPresent>
