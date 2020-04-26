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
package org.fenixedu.academic.service.services.teacher.onlineTests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.onlineTests.Question;
import org.fenixedu.academic.domain.onlineTests.SubQuestion;
import org.fenixedu.academic.domain.onlineTests.Test;
import org.fenixedu.academic.domain.onlineTests.TestQuestion;
import org.fenixedu.academic.domain.onlineTests.utils.ParseSubQuestion;
import org.fenixedu.academic.dto.onlineTests.InfoDistributedTest;
import org.fenixedu.academic.dto.onlineTests.InfoSiteStudentTestFeedback;
import org.fenixedu.academic.dto.onlineTests.InfoStudentTestQuestion;
import org.fenixedu.academic.dto.onlineTests.InfoTestScope;
import org.fenixedu.academic.service.filter.ExecutionCourseLecturingTeacherAuthorizationFilter;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.service.services.exceptions.InvalidArgumentsServiceException;
import org.fenixedu.academic.service.services.exceptions.NotAuthorizedException;
import org.fenixedu.academic.service.strategy.tests.QuestionCorrectionStrategyFactory;
import org.fenixedu.academic.service.strategy.tests.strategys.IQuestionCorrectionStrategy;
import org.fenixedu.academic.util.tests.CorrectionAvailability;
import org.fenixedu.academic.util.tests.QuestionType;
import org.fenixedu.academic.util.tests.Response;
import org.fenixedu.academic.util.tests.ResponseLID;
import org.fenixedu.academic.util.tests.ResponseNUM;
import org.fenixedu.academic.util.tests.ResponseProcessing;
import org.fenixedu.academic.util.tests.ResponseSTR;
import org.fenixedu.academic.util.tests.TestType;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class SimulateTest {

    private final String path = new String();

    protected InfoSiteStudentTestFeedback run(String executionCourseId, String testId, Response[] responses, String[] questionCodes,
            String[] optionShuffle, TestType testType, CorrectionAvailability correctionAvailability, Boolean imsfeedback, String testInformation,
            String path) throws FenixServiceException {

        InfoSiteStudentTestFeedback infoSiteStudentTestFeedback = new InfoSiteStudentTestFeedback();

        Test test = FenixFramework.getDomainObject(testId);
        if (test == null)
            throw new FenixServiceException();

        double totalMark = 0;
        int responseNumber = 0;
        int notResponseNumber = 0;
        List<String> errors = new ArrayList<String>();

        ExecutionCourse executionCourse = FenixFramework.getDomainObject(executionCourseId);
        if (executionCourse == null) {
            throw new InvalidArgumentsServiceException();
        }

        InfoDistributedTest infoDistributedTest = new InfoDistributedTest();
        infoDistributedTest.setExternalId(testId);
        infoDistributedTest.setInfoTestScope(InfoTestScope.newInfoFromDomain(executionCourse.getTestScope()));
        infoDistributedTest.setTestType(testType);
        infoDistributedTest.setCorrectionAvailability(correctionAvailability);
        infoDistributedTest.setImsFeedback(imsfeedback);
        infoDistributedTest.setTestInformation(testInformation);
        infoDistributedTest.setTitle(test.getTitle());
        infoDistributedTest.setNumberOfQuestions(test.getTestQuestionsSet().size());

        List<InfoStudentTestQuestion> infoStudentTestQuestionList =
                getInfoStudentTestQuestionList(questionCodes, optionShuffle, responses, infoDistributedTest, testId);
        if (infoStudentTestQuestionList.size() == 0) {
            return null;
        }
        for (InfoStudentTestQuestion infoStudentTestQuestion : infoStudentTestQuestionList) {
            if (infoStudentTestQuestion.getResponse().isResponsed()) {
                responseNumber++;

                IQuestionCorrectionStrategy questionCorrectionStrategy =
                        QuestionCorrectionStrategyFactory.getInstance().getQuestionCorrectionStrategy(infoStudentTestQuestion);

                String error = questionCorrectionStrategy.validResponse(infoStudentTestQuestion, infoStudentTestQuestion.getResponse());
                if (error == null) {
                    if ((!infoDistributedTest.getTestType().equals(new TestType(TestType.INQUIRY)))
                            && infoStudentTestQuestion.getSubQuestionByItem().getResponseProcessingInstructions().size() != 0) {

                        infoStudentTestQuestion = (InfoStudentTestQuestion) questionCorrectionStrategy.getMark(infoStudentTestQuestion);
                    }
                    totalMark += infoStudentTestQuestion.getTestQuestionMark().doubleValue();
                } else {
                    notResponseNumber++;
                    responseNumber--;
                    errors.add(error);
                    if (infoStudentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.LID)
                        infoStudentTestQuestion.setResponse(new ResponseLID());
                    else if (infoStudentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.STR)
                        infoStudentTestQuestion.setResponse(new ResponseSTR());
                    else if (infoStudentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.NUM)
                        infoStudentTestQuestion.setResponse(new ResponseNUM());
                }
            } else
                notResponseNumber++;
        }

        infoSiteStudentTestFeedback.setResponseNumber(Integer.valueOf(responseNumber));
        infoSiteStudentTestFeedback.setNotResponseNumber(Integer.valueOf(notResponseNumber));
        infoSiteStudentTestFeedback.setErrors(errors);

        infoSiteStudentTestFeedback.setStudentTestQuestionList(infoStudentTestQuestionList);
        return infoSiteStudentTestFeedback;
    }

    private SubQuestion correctQuestionValues(SubQuestion subQuestion, Double questionValue) {
        Double maxValue = Double.valueOf(0);
        for (ResponseProcessing responseProcessing : subQuestion.getResponseProcessingInstructions()) {
            if (responseProcessing.getAction().intValue() == ResponseProcessing.SET
                    || responseProcessing.getAction().intValue() == ResponseProcessing.ADD) {
                if (maxValue.compareTo(responseProcessing.getResponseValue()) < 0) {
                    maxValue = responseProcessing.getResponseValue();
                }
            }
        }
        if (maxValue.compareTo(questionValue) != 0) {
            double difValue = questionValue * Math.pow(maxValue, -1);
            for (ResponseProcessing responseProcessing : subQuestion.getResponseProcessingInstructions()) {
                responseProcessing.setResponseValue(Double.valueOf(responseProcessing.getResponseValue() * difValue));
            }
        }

        return subQuestion;
    }

    private List<InfoStudentTestQuestion> getInfoStudentTestQuestionList(String[] questionCodes, String[] optionShuffle, Response[] responses,
            InfoDistributedTest infoDistributedTest, String testId) throws InvalidArgumentsServiceException, FenixServiceException {
        List<InfoStudentTestQuestion> infoStudentTestQuestionList = new ArrayList<InfoStudentTestQuestion>();

        Test test = FenixFramework.getDomainObject(testId);
        List<TestQuestion> testQuestionList = new ArrayList<TestQuestion>(test.getTestQuestionsSet());
        Collections.sort(testQuestionList, new BeanComparator("testQuestionOrder"));
        for (int i = 0; i < testQuestionList.size(); i++) {
            TestQuestion testQuestionExample = testQuestionList.get(i);
            InfoStudentTestQuestion infoStudentTestQuestion = new InfoStudentTestQuestion();
            infoStudentTestQuestion.setDistributedTest(infoDistributedTest);
            infoStudentTestQuestion.setTestQuestionOrder(testQuestionExample.getTestQuestionOrder());
            infoStudentTestQuestion.setTestQuestionValue(testQuestionExample.getTestQuestionValue());
            infoStudentTestQuestion.setOldResponse(Integer.valueOf(0));
            infoStudentTestQuestion.setCorrectionFormula(testQuestionExample.getCorrectionFormula());
            infoStudentTestQuestion.setTestQuestionMark(Double.valueOf(0));
            infoStudentTestQuestion.setResponse(null);
            Question question = FenixFramework.getDomainObject(questionCodes[i]);
            if (question == null) {
                throw new InvalidArgumentsServiceException();
            }
            infoStudentTestQuestion.setQuestion(question);
            ParseSubQuestion parse = new ParseSubQuestion();
            try {
                infoStudentTestQuestion.setOptionShuffle(optionShuffle[i]);
                infoStudentTestQuestion = parse.parseStudentTestQuestion(infoStudentTestQuestion, infoDistributedTest.getTestType());
//                infoStudentTestQuestion.set(correctQuestionValues(infoStudentTestQuestion.getSubQuestionByItem(),
//                        Double.valueOf(infoStudentTestQuestion.getTestQuestionValue())));
                correctQuestionValues(infoStudentTestQuestion.getSubQuestionByItem(), Double.valueOf(infoStudentTestQuestion.getTestQuestionValue()));

                infoStudentTestQuestion.setResponse(responses[i]);

            } catch (Exception e) {
                throw new FenixServiceException(e);
            }
            infoStudentTestQuestionList.add(infoStudentTestQuestion);
        }
        return infoStudentTestQuestionList;
    }

    // Service Invokers migrated from Berserk

    private static final SimulateTest serviceInstance = new SimulateTest();

    @Atomic
    public static InfoSiteStudentTestFeedback runSimulateTest(String executionCourseId, String testId, Response[] responses, String[] questionCodes,
            String[] optionShuffle, TestType testType, CorrectionAvailability correctionAvailability, Boolean imsfeedback, String testInformation,
            String path) throws FenixServiceException, NotAuthorizedException {
        ExecutionCourseLecturingTeacherAuthorizationFilter.instance.execute(executionCourseId);
        return serviceInstance.run(executionCourseId, testId, responses, questionCodes, optionShuffle, testType, correctionAvailability, imsfeedback,
                testInformation, path);
    }

}