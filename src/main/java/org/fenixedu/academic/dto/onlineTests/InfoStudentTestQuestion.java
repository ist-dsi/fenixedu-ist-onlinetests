/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Core.
 *
 * FenixEdu Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Core.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on 28/Ago/2003
 */
package org.fenixedu.academic.dto.onlineTests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fenixedu.academic.domain.onlineTests.DistributedTest;
import org.fenixedu.academic.domain.onlineTests.Question;
import org.fenixedu.academic.domain.onlineTests.StudentTestQuestion;
import org.fenixedu.academic.domain.onlineTests.SubQuestion;
import org.fenixedu.academic.dto.InfoObject;
import org.fenixedu.academic.dto.InfoStudent;
import org.fenixedu.academic.util.tests.CorrectionFormula;
import org.fenixedu.academic.util.tests.Response;

/**
 * @author Susana Fernandes
 */
public class InfoStudentTestQuestion implements IStudentTestQuestion, Serializable  {

    private static InheritableThreadLocal<Map<InfoStudentTestQuestion, List<SubQuestion>>> studentSubQuestions =
            new InheritableThreadLocal<Map<InfoStudentTestQuestion, List<SubQuestion>>>();

    private InfoDistributedTest distributedTest;

    private Question question;

    private Integer testQuestionOrder;

    private Double testQuestionValue;

    private Integer oldResponse;

    private String optionShuffle;

    private Double testQuestionMark;

    private Response response;

    private CorrectionFormula formula;
    
    private String itemId;

    public InfoStudentTestQuestion() {
    }

    public InfoDistributedTest getDistributedTest() {
        return distributedTest;
    }

    public String getOptionShuffle() {
        return optionShuffle;
    }

    public Question getQuestion() {
        return question;
    }

    public Integer getOldResponse() {
        return oldResponse;
    }

    public Integer getTestQuestionOrder() {
        return testQuestionOrder;
    }

    public Double getTestQuestionValue() {
        return testQuestionValue;
    }

    public Double getTestQuestionMark() {
        return testQuestionMark;
    }

    public Response getResponse() {
        return response;
    }

    public void setDistributedTest(InfoDistributedTest test) {
        distributedTest = test;
    }

    public void setOptionShuffle(String string) {
        optionShuffle = string;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public void setOldResponse(Integer response) {
        this.oldResponse = response;
    }

    public void setTestQuestionOrder(Integer integer) {
        testQuestionOrder = integer;
    }

    public void setTestQuestionValue(Double value) {
        testQuestionValue = value;
    }

    public void setTestQuestionMark(Double double1) {
        testQuestionMark = double1;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public CorrectionFormula getCorrectionFormula() {
        return formula;
    }

    public void setCorrectionFormula(CorrectionFormula formula) {
        this.formula = formula;
    }
    
    public List<SubQuestion> getStudentSubQuestions() {
        if (studentSubQuestions.get() == null) {
            studentSubQuestions.set(new HashMap<InfoStudentTestQuestion, List<SubQuestion>>());
        }

        return studentSubQuestions.get().get(this);
    }

    public void setStudentSubQuestions(List<SubQuestion> studentSubQuestions) {
        getStudentSubQuestions();
        this.studentSubQuestions.get().put(this, studentSubQuestions);
    }
    
    public void addStudentSubQuestion(SubQuestion subQuestion) {
        if (getStudentSubQuestions() == null) {
            setStudentSubQuestions(new ArrayList<SubQuestion>());
        }
        getStudentSubQuestions().add(subQuestion);
    }
    

    public SubQuestion getSubQuestionByItem() {
        if (getItemId() == null && !getStudentSubQuestions().isEmpty() && getStudentSubQuestions().size() == 1) {
            return getStudentSubQuestions().iterator().next();
        }
        for (SubQuestion subQuestion : getStudentSubQuestions()) {
            if (getItemId() != null && subQuestion.getItemId() != null) {
                if (getItemId().equals(subQuestion.getItemId())) {
                    return subQuestion;
                }
            } else if (getItemId() == null && subQuestion.getItemId() == null) {
                return subQuestion;
            }
        }
        return null;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }


}