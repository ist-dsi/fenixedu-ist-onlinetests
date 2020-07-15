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
 * Created on 24/Jul/2003
 *
 */
package org.fenixedu.academic.domain.onlineTests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fenixedu.academic.domain.onlineTests.utils.ParseSubQuestion;
import org.fenixedu.bennu.core.domain.Bennu;

/**
 * @author Susana Fernandes
 */
public class Question extends Question_Base {

    public Question() {
        super();
        setRootDomainObject(Bennu.getInstance());
    }

    public Question(String fileName, String xmlFile, boolean visibility) {
        this();
        setVisibility(visibility);
        setQuestionFile(new QuestionFile(this, fileName, xmlFile));
    }

    public void replaceTestQuestions() {
        Question newQuestion = getMetadata().getVisibleQuestions().stream().filter(q -> !q.equals(this)).findAny().orElse(null);
        getTestQuestionsSet().forEach(testQuestion -> {
            if (newQuestion != null) {
                testQuestion.setQuestion(newQuestion);
            } else {
                testQuestion.getTest().deleteTestQuestion(testQuestion);
            }
        });
    }
    
    public void deleteIfNotUsed() {
        if(!getVisibility() && getStudentTestsQuestionsSet().isEmpty()){
            Metadata metadata = getMetadata();
            delete();
            metadata.deleteIfHasNoQuestions();
        }
    }
    
    public void delete() {
        getTestQuestionsSet().forEach(testQuestion -> testQuestion.getTest().deleteTestQuestion(testQuestion));
        if (!getStudentTestsQuestionsSet().isEmpty()) {
            setVisibility(false);
        } else {
            setMetadata(null);
            setRootDomainObject(null);
            QuestionFile qf = getQuestionFile();
            if (qf != null) {
                qf.delete();
            }
            super.deleteDomainObject();
        }
    }

    public Set<DistributedTest> findDistributedTests() {
        final Set<DistributedTest> distributedTests = new HashSet<DistributedTest>();
        for (final StudentTestQuestion studentTestQuestion : getStudentTestsQuestionsSet()) {
            distributedTests.add(studentTestQuestion.getDistributedTest());
        }
        return distributedTests;
    }

    public List<SubQuestion> getSubQuestions() {
        return ParseSubQuestion.getSubQuestionFor(this);
    }

    @Override
    public String getXmlFile() {
        final QuestionFile questionFile = getQuestionFile();
        return questionFile == null ? super.getXmlFile() : questionFile.getXml();
    }

    @Override
    public String getXmlFileName() {
        if (getQuestionFile() != null) {
            return getQuestionFile().getFilename();
        }
        return super.getXmlFileName();
    }

    protected void ensureFileInitialized() {
        if (getQuestionFile() == null) {
            setQuestionFile(new QuestionFile(this, getXmlFileName(), getXmlFile()));
        }
        setXmlFile(null);
        setXmlFileName(null);
    }

}
