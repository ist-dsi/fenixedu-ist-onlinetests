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
<%@ page language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<html:xhtml/>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<jsp:include page="/includeMathJax.jsp" />
<logic:present name="studentTestQuestionList">
	
	<logic:empty name="studentTestQuestionList">
		<h2><bean:message key="message.test.no.available" /></h2>
	</logic:empty> 
	
	<logic:notEmpty name="studentTestQuestionList">

		<html:form action="/studentTestManagement">
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.method" property="method" value="showTestMarks" />

			<logic:iterate id="testQuestion" name="studentTestQuestionList"
				type="org.fenixedu.academic.domain.onlineTests.StudentTestQuestion" />
			<bean:define id="distributedTest" name="testQuestion" property="distributedTest"
				type="org.fenixedu.academic.domain.onlineTests.DistributedTest" />
			<bean:define id="testCode" name="distributedTest" property="externalId" />

			<bean:define id="objectCode" name="distributedTest" property="testScope.executionCourse.externalId" />
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.objectCode" property="objectCode" value="<%= objectCode.toString() %>" />
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.distributedTestCode" property="distributedTestCode" value="<%= testCode.toString() %>" />
			<center>
				<h2><bean:write name="distributedTest" property="title" /></h2>
				<b><bean:write name="distributedTest" property="testInformation" /></b>
			</center>
			<br />
			<br />
			<bean:define id="testType" name="distributedTest" property="testType.type" />
			<%if(((Integer)testType).intValue()!=3){%>
			<b><bean:message key="label.test.totalClassification"/>:</b>&nbsp;<bean:write name="classification"/>
			<%}%>
			
			<bean:define id="testType" name="distributedTest" property="testType.type"/>
			<bean:define id="correctionAvailability" name="distributedTest" property="correctionAvailability.availability"/>
			<bean:define id="imsFeedback" name="distributedTest" property="imsFeedback"/>
			<jsp:include page="showStudentTest.jsp">
				<jsp:param name="pageType" value="correction"/>
				<jsp:param name="correctionType" value="studentCorrection"/>
				<jsp:param name="testCode" value="<%=testCode%>"/>
				<jsp:param name="testType" value="<%=testType%>"/>
				<jsp:param name="correctionAvailability" value="<%=correctionAvailability%>"/>
				<jsp:param name="imsFeedback" value="<%=imsFeedback%>"/>
		 	</jsp:include>
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