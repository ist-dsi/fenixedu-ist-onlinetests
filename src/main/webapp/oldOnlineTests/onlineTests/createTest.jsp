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
<h2><bean:message key="link.createTest"/></h2>

<logic:present name="availableMetadatas">
	<logic:equal name="availableMetadatas" value="0">
		<span class="error"><!-- Error messages go here --><bean:message key="message.tests.no.exercises"/></span>
	</logic:equal>
	
	<logic:notEqual name="availableMetadatas" value="0">
		<html:form action="/testsManagement" styleClass="form-horizontal">
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.page" property="page" value="1"/>
			<html:hidden bundle="HTMLALT_RESOURCES" altKey="hidden.method" property="method" value="createTest"/>
			<input type="hidden" name="executionCourseID" value="${executionCourseID}" />
			<table><tr><td class="infoop"><bean:message key="message.createTest.information" /></td></tr></table>
			<br/><br/>
			<div class="form-group">
				<label class="control-label col-sm-2"><bean:message key="label.test.title"/></label>
				<div class="col-sm-10">
					<html:text bundle="HTMLALT_RESOURCES" altKey="text.title" size="75" property="title"/><span class="error"><!-- Error messages go here --><html:errors /></span>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2"><bean:message key="label.test.information"/></label>
				<div class="col-sm-10">
					<html:textarea bundle="HTMLALT_RESOURCES" altKey="textarea.information" rows="7" cols="75" property="information"/>
				</div>
			</div>
			<div class="form-group">
				<span class="col-sm-2">
				</span>
				<span class="col-sm-10">
					<html:submit bundle="HTMLALT_RESOURCES" altKey="submit.submit" styleClass="inputbutton"><bean:message key="button.continue"/></html:submit>
					<html:reset bundle="HTMLALT_RESOURCES" altKey="reset.reset" styleClass="inputbutton"><bean:message key="label.clear"/></html:reset>
				</span>
			</div>
		</html:form>
	</logic:notEqual>
</logic:present>
<logic:notPresent name="availableMetadatas">
	<span class="error"><!-- Error messages go here --><bean:message key="message.tests.no.exercises"/></span>
</logic:notPresent>