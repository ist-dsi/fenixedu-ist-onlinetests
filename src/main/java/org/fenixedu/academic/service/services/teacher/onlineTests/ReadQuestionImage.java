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
 * Created on 4/Ago/2003
 *  
 */
package org.fenixedu.academic.service.services.teacher.onlineTests;

import org.fenixedu.academic.domain.onlineTests.DistributedTest;
import org.fenixedu.academic.domain.onlineTests.Question;
import org.fenixedu.academic.domain.onlineTests.utils.ParseSubQuestion;
import org.fenixedu.academic.dto.onlineTests.InfoStudentTestQuestion;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.utils.ParseQuestionException;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

/**
 * @author Susana Fernandes
 */
public class ReadQuestionImage {

    @Atomic
    public static String run(String exerciseId, Integer imageId, Integer feedbackId, Integer itemIndex)
            throws FenixServiceException {
        Question question = FenixFramework.getDomainObject(exerciseId);
        if (question != null) {
            if (question.getSubQuestions() == null || question.getSubQuestions().size() == 0) {
                ParseSubQuestion parse = new ParseSubQuestion();
                try {
                    question = parse.parseSubQuestion(question);
                } catch (ParseQuestionException e) {
                    throw new FenixServiceException();
                }
            }
            if (question.getSubQuestions().size() < itemIndex) {
                return null;
            }
            return question.getSubQuestions().get(itemIndex).getImage(imageId, feedbackId);
        }
        return null;
    }

    @Atomic
    public static String run(String distributedTestId, String questionId, String optionShuffle, Integer imageId,
            Integer feedbackId) throws FenixServiceException {

        DistributedTest distributedTest = FenixFramework.getDomainObject(distributedTestId);
        Question question = FenixFramework.getDomainObject(questionId);
        if (question == null) {
            throw new FenixServiceException("Unexisting Question!!!!!");
        }

        InfoStudentTestQuestion infoStudentTestQuestion = new InfoStudentTestQuestion();
        try {
            ParseSubQuestion parse = new ParseSubQuestion();
            infoStudentTestQuestion.setQuestion(question);
            infoStudentTestQuestion.setOptionShuffle(optionShuffle);
            infoStudentTestQuestion = parse.parseStudentTestQuestion(infoStudentTestQuestion, distributedTest.getTestType());
        } catch (Exception e) {
            throw new FenixServiceException(e);
        }
        if (question.getSubQuestions().size() < imageId) {
            return null;
        }
        return question.getSubQuestions().get(imageId).getImage(imageId, feedbackId);
    }
}