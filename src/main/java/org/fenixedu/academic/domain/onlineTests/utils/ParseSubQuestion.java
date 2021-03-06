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
 * Created on 25/Jul/2003
 */
package org.fenixedu.academic.domain.onlineTests.utils;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.struts.util.LabelValueBean;
import org.fenixedu.academic.domain.onlineTests.Question;
import org.fenixedu.academic.domain.onlineTests.StudentTestQuestion;
import org.fenixedu.academic.domain.onlineTests.SubQuestion;
import org.fenixedu.academic.dto.onlineTests.InfoStudentTestQuestion;
import org.fenixedu.academic.util.tests.CardinalityType;
import org.fenixedu.academic.util.tests.QuestionOption;
import org.fenixedu.academic.util.tests.QuestionType;
import org.fenixedu.academic.util.tests.RenderChoise;
import org.fenixedu.academic.util.tests.RenderFIB;
import org.fenixedu.academic.util.tests.ResponseCondition;
import org.fenixedu.academic.util.tests.ResponseProcessing;
import org.fenixedu.academic.util.tests.TestType;
import org.fenixedu.academic.utils.Element;
import org.fenixedu.academic.utils.ParseQuestionException;
import org.fenixedu.academic.utils.QuestionResolver;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Susana Fernandes
 */
public class ParseSubQuestion extends DefaultHandler {

    private String text;

    private Element current = null;

    private List<QuestionElement> questionElementList;

    private boolean questionPresentation = false, question = false, option = false, response = false, feedback = false, invalidFeedback = false;

    private static final Element NOT_ELEMENT = new Element(null, "not", "not", null);

    private static final Element SLASH_NOT_ELEMENT = new Element(null, "/not", "/not", null);

    public Question parseSubQuestion(Question question) throws ParseQuestionException {
        if (question.getSubQuestions() == null || question.getSubQuestions().size() == 0) {
            try {
                parseFile(question.getXmlFile());
            } catch (Exception e) {
                throw new ParseQuestionException(e);
            }
            List<SubQuestion> subQuestions = new ArrayList<SubQuestion>();
            for (QuestionElement questionElement : questionElementList) {
                subQuestions.add(createSubQuestion(questionElement));
            }
            setSubQuestionFor(question, subQuestions);
        }
        return question;
    }

    // for the preview, only has 1 item
    public SubQuestion parseSubQuestion(String fileString) throws ParseQuestionException {
        try {
            parseFile(fileString);
        } catch (Exception e) {
            throw new ParseQuestionException(e);
        }
        SubQuestion subQuestion = createSubQuestion(questionElementList.iterator().next());
        if (invalidFeedback) {
            throw new ParseQuestionException();
        }
        return subQuestion;
    }

    public StudentTestQuestion parseStudentTestQuestion(StudentTestQuestion studentTestQuestion) throws Exception, ParseQuestionException {
        if (studentTestQuestion.getStudentSubQuestions() == null || studentTestQuestion.getStudentSubQuestions().size() == 0) {
            try {
                parseFile(studentTestQuestion.getQuestion().getXmlFile());
            } catch (Exception e) {
                throw new ParseQuestionException(e);
            }
            for (QuestionElement questionElement : questionElementList) {
                studentTestQuestion.addStudentSubQuestion(createSubQuestion(questionElement));
            }
            SubQuestion subQuestion = studentTestQuestion.getSubQuestionByItem();
            if (!studentTestQuestion.getDistributedTest().getTestType().equals(new TestType(3))
                    && subQuestion.getQuestionType().getType().intValue() == QuestionType.LID) {
                String optionShuffle = studentTestQuestion.getOptionShuffle();
                if (optionShuffle == null || optionShuffle.length() == 0) {
                    if (subQuestion.getShuffle() == null) {
                        subQuestion.setShuffle(shuffleOptions(getQuestionElement(studentTestQuestion.getItemId())));
                    }
                } else {
                    subQuestion.setShuffle(optionShuffle.substring(1, optionShuffle.length() - 1).split(","));
                }
                subQuestion.setOptions(shuffleStudentTestQuestionOptions(subQuestion.getShuffle(), subQuestion.getOptions()));
                subQuestion.setResponseProcessingInstructions(shuffleStudentTestQuestionResponses(subQuestion.getShuffle(),
                        subQuestion.getResponseProcessingInstructions()));
            }
        }
        return studentTestQuestion;
    }

    public InfoStudentTestQuestion parseStudentTestQuestion(InfoStudentTestQuestion infoStudentTestQuestion, TestType testType) throws Exception, ParseQuestionException {
        if (infoStudentTestQuestion.getStudentSubQuestions() == null || infoStudentTestQuestion.getStudentSubQuestions().size() == 0) {
            try {
                parseFile(infoStudentTestQuestion.getQuestion().getXmlFile());
            } catch (Exception e) {
                throw new ParseQuestionException(e);
            }
            for (QuestionElement questionElement : questionElementList) {
                infoStudentTestQuestion.addStudentSubQuestion(createSubQuestion(questionElement));
            }
            SubQuestion subQuestion = infoStudentTestQuestion.getSubQuestionByItem();
            if (!testType.equals(new TestType(3))
                    && subQuestion.getQuestionType().getType().intValue() == QuestionType.LID) {
                String optionShuffle = infoStudentTestQuestion.getOptionShuffle();
                if (optionShuffle == null || optionShuffle.length() == 0) {
                    if (subQuestion.getShuffle() == null) {
                        subQuestion.setShuffle(shuffleOptions(getQuestionElement(infoStudentTestQuestion.getItemId())));
                    }
                } else {
                    subQuestion.setShuffle(optionShuffle.substring(1, optionShuffle.length() - 1).split(","));
                }
                subQuestion.setOptions(shuffleStudentTestQuestionOptions(subQuestion.getShuffle(), subQuestion.getOptions()));
                subQuestion.setResponseProcessingInstructions(shuffleStudentTestQuestionResponses(subQuestion.getShuffle(),
                        subQuestion.getResponseProcessingInstructions()));
            }
        }
        return infoStudentTestQuestion;
    }

    private QuestionElement getQuestionElement(String questionItem) {
        if (!questionElementList.isEmpty() && questionElementList.size() == 1 && questionItem == null) {
            return questionElementList.iterator().next();
        }
        for (QuestionElement questionElement : questionElementList) {
            if (questionElement.getItemId().equals(questionItem)) {
                return questionElement;
            }
        }
        return null;
    }

    public void parseFile(String file) throws ParserConfigurationException, IOException, SAXException {
        questionElementList = new ArrayList<QuestionElement>();
        questionPresentation = false;
        question = false;
        option = false;
        response = false;
        feedback = false;
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(true);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader reader = saxParser.getXMLReader();
        reader.setContentHandler(this);
        reader.setErrorHandler(this);
        StringReader sr = new StringReader(file);
        InputSource input = new InputSource(sr);
        QuestionResolver resolver = new QuestionResolver();
        reader.setEntityResolver(resolver);
        reader.parse(input);
    }

    @Override
    public void error(SAXParseException e) throws SAXParseException {
        throw e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXParseException {
        throw e;
    }

    @Override
    public void warning(SAXParseException e) throws SAXParseException {
        throw e;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        current = new Element(uri, localName, qName, new AttributesImpl(attributes));

        // if (itemId == null && !doneItem) {
        // item = true;
        // }
        // if (itemId != null && qName.equals("item") &&
        // attributes.getValue("ident").equals(itemId)) {
        // item = true;
        // }
        // if (item) {

        if (qName.equals("section")) {
            questionPresentation = true;
            question = false;
            if (questionElementList.size() == 0 || questionElementList.get(questionElementList.size() - 1).getListQuestion().size() != 0) {
                questionElementList.add(new QuestionElement());
            }
        } else if (qName.equals("item")) {
            questionPresentation = false;
            question = true;
            if (questionElementList.size() == 0 || questionElementList.get(questionElementList.size() - 1).getItemId() != null) {
                questionElementList.add(new QuestionElement());
            }
            questionElementList.get(questionElementList.size() - 1).setItemId(attributes.getValue("ident"));
            if (attributes.getIndex("title") != -1) {
                questionElementList.get(questionElementList.size() - 1).setTitle(attributes.getValue("title"));
            }

        } else if (qName.equals("response_lid") || qName.equals("response_str") || qName.equals("response_num")) {
            question = false;
            option = true;
        } else if (qName.equals("resprocessing")) {
            response = true;
            question = false;
            option = false;
            questionPresentation = false;
        } else if (qName.equals("itemfeedback")) {
            feedback = true;
            response = false;
            question = false;
            option = false;
            questionPresentation = false;
        }
        if (questionPresentation) {
            questionElementList.get(questionElementList.size() - 1).addListQuestionPresentation(current);
        } else if (question) {
            questionElementList.get(questionElementList.size() - 1).addListQuestion(current);
        } else if (option) {
            questionElementList.get(questionElementList.size() - 1).addListOptions(current);
        } else if (response) {
            questionElementList.get(questionElementList.size() - 1).addListResponse(current);
        } else if (feedback) {
            questionElementList.get(questionElementList.size() - 1).addListFeedback(current);
        }
        // }
        text = "";
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (current != null && text != null) {
            current.setValue(text.trim());
        }
        current = null;
        // if (item) {
        if (qName.equals("response_lid")) {
            option = false;
            question = true;
        } else if (qName.equals("not") || qName.equals("and") || qName.equals("or")) {
            questionElementList.get(questionElementList.size() - 1).addListResponse(new Element(uri, localName, "/" + qName, null));
        } else if (qName.equals("itemfeedback")) {
            questionElementList.get(questionElementList.size() - 1).addListFeedback(new Element(uri, localName, "/" + qName, null));
            // } else if (qName.equals("item")) {
            // item = false;
            // doneItem = true;
            // }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (current != null && text != null) {
            String value = new String(ch, start, length);
            text += value;
        }
    }

    private SubQuestion createSubQuestion(QuestionElement questionElement) throws ParseQuestionException {
        SubQuestion subQuestion = new SubQuestion();
        subQuestion.setItemId(questionElement.getItemId());
        subQuestion.setTitle(questionElement.getTitle());
        subQuestion.setPrePresentation(getPresentation(questionElement.getListQuestionPresentation(), subQuestion));
        subQuestion.setPresentation(getPresentation(questionElement.getListQuestion(), subQuestion));
        subQuestion = getOptions(questionElement, subQuestion);
        subQuestion = getResponseProcessingInstructions(questionElement, subQuestion);
        subQuestion = getFeedback(questionElement, subQuestion);

        if (subQuestion.getQuestionType() != null && subQuestion.getQuestionType().getType().intValue() == QuestionType.LID) {
            subQuestion = getRidOfEmptyResponseConditions(subQuestion);
        }
        subQuestion = setFenixCorrectResponse(subQuestion);
        subQuestion = removeRepeatedConditions(subQuestion);
        if (subQuestion.getQuestionType() != null && subQuestion.getQuestionType().getType().intValue() == QuestionType.LID) {
            newResponseList(subQuestion.getResponseProcessingInstructions(), subQuestion.getOptions());
        }

        return subQuestion;
    }

    private List<LabelValueBean> getPresentation(List<Element> questionList, SubQuestion subQuestion) throws ParseQuestionException {
        List<LabelValueBean> presentationList = new ArrayList<LabelValueBean>();
        for (Element element : questionList) {
            String tag = element.getQName();
            Attributes atts = element.getAttributes();
            if (tag.startsWith("render_")) {
                if (!(tag.equals("render_choice") || tag.equals("render_fib"))) {
                    throw new ParseQuestionException(tag, true);
                }
            } else if (tag.startsWith("mat") && !tag.equals("material")) {
                if ((tag.equals("mattext"))) {
                    String textType = "text/plain";
                    if (atts.getIndex("texttype") != -1) {
                        textType = atts.getValue("texttype");
                    }
                    presentationList.add(new LabelValueBean(textType, element.getValue()));
                } else if ((tag.equals("matimage"))) {
                    if (atts.getIndex("label") != -1) {
                        presentationList.add(new LabelValueBean("image_label", atts.getValue("label")));
                    }
                    if (atts.getIndex("uri") != -1) {
                        throw new ParseQuestionException(tag, "uri");
                    }
                    presentationList.add(new LabelValueBean(atts.getValue("imagtype"), element.getValue()));
                } else {
                    throw new ParseQuestionException(tag, true);
                }
            } else if ((tag.startsWith("flow"))) {
                presentationList.add(new LabelValueBean("flow", ""));
            }
        }
        return presentationList;
    }

    private SubQuestion getOptions(QuestionElement questionElement, SubQuestion subQuestion) throws ParseQuestionException {
        List<LabelValueBean> optionsAuxList = new ArrayList<LabelValueBean>();
        int optionNumber = 0;
        // int questions = 0;
        QuestionOption questionOption = new QuestionOption();
        List<QuestionOption> optionList = new ArrayList<QuestionOption>();
        for (Element element : questionElement.getListOptions()) {
            String tag = element.getQName();
            Attributes atts = element.getAttributes();
            if (tag.startsWith("render_")) {
                if (tag.equals("render_fib")) {
                    RenderFIB renderFIB = new RenderFIB();
                    renderFIB.setFibtype(atts.getValue("fibtype"));
                    if (atts.getIndex("rows") != -1) {
                        renderFIB.setRows(Integer.valueOf(atts.getValue("rows")));
                    }
                    if (atts.getIndex("columns") != -1) {
                        renderFIB.setColumns(Integer.valueOf(atts.getValue("columns")));
                    }
                    if (atts.getIndex("maxchars") != -1) {
                        renderFIB.setMaxchars(Integer.valueOf(atts.getValue("maxchars")));
                    }
                    subQuestion.getQuestionType().setRender(renderFIB);
                } else if (tag.equals("render_choice")) {
                    RenderChoise renderChoise = new RenderChoise();
                    renderChoise.setShuffle(atts.getValue("shuffle"));
                    subQuestion.getQuestionType().setRender(renderChoise);
                } else {
                    throw new ParseQuestionException(tag, true);
                }
            } else if ((tag.startsWith("mat") && !tag.equals("material")) || tag.startsWith("response_")) {
                if ((tag.equals("response_lid")) || (tag.equals("response_str")) || (tag.equals("response_num"))) {
                    questionElement.setResponseId(atts.getValue("ident"));
                    // questions++;
                    // if (questions > 1)
                    // throw new ParseQuestionException("O sistema ainda nÃ£o
                    // suporta perguntas com alÃ­neas.");

                    subQuestion.setQuestionType(new QuestionType(tag));

                    if (atts.getIndex("rcardinality") != -1) {
                        if (atts.getValue("rcardinality").equals("Ordered")) {
                            throw new ParseQuestionException(tag, "rcardinality=Ordered");
                        }
                        subQuestion.getQuestionType().setCardinalityType(new CardinalityType(atts.getValue("rcardinality")));
                    }
                } else if ((tag.equals("response_label")) || (tag.equals("response_na"))) {

                    if (questionOption.getOptionId() == null) {
                        questionOption.setOptionId(atts.getValue("ident"));
                    } else {
                        if (optionsAuxList.size() != 0) {
                            questionOption.setOptionContent(optionsAuxList);
                            optionList.add(questionOption);
                        }
                        questionOption = new QuestionOption(atts.getValue("ident"));
                        optionsAuxList = new ArrayList<LabelValueBean>();
                    }
                    if (tag.equals("response_na")) {
                        questionOption.setEmptyResponse(true);
                    }

                    optionsAuxList.add(new LabelValueBean("response_label", atts.getValue("ident")));
                    optionNumber++;
                } else if ((tag.equals("mattext"))) {
                    String textType = "text/plain";
                    if (atts.getIndex("texttype") != -1) {
                        textType = atts.getValue("texttype");
                    }
                    optionsAuxList.add(new LabelValueBean(textType, element.getValue()));
                } else if ((tag.equals("matimage"))) {
                    if (atts.getIndex("label") != -1) {
                        optionsAuxList.add(new LabelValueBean("image_label", atts.getValue("label")));
                    }
                    if (atts.getIndex("uri") != -1) {
                        throw new ParseQuestionException(tag, "uri");
                    }
                    optionsAuxList.add(new LabelValueBean(atts.getValue("imagtype"), element.getValue()));
                } else {
                    throw new ParseQuestionException(tag, true);
                }
            } else if ((tag.equals("flow"))) {
                optionsAuxList.add(new LabelValueBean("flow", ""));
            }
        }
        if (questionOption != null && optionsAuxList.size() != 0) {
            questionOption.setOptionContent(optionsAuxList);
            optionList.add(questionOption);
        }
        subQuestion.setOptionNumber(Integer.valueOf(optionNumber));
        subQuestion.setOptions(optionList);

        return subQuestion;
    }

    private SubQuestion getResponseProcessingInstructions(QuestionElement questionElement, SubQuestion subQuestion) throws ParseQuestionException {
        List<Element> newResponseList = getRidOfNot(questionElement);
        ListIterator<Element> it = newResponseList.listIterator();
        List<ResponseProcessing> auxList = new ArrayList<ResponseProcessing>();
        ResponseProcessing responseProcessing = null;
        int responseProcessingId = 0, and = 0, or = 0;
        boolean not = false;
        for (int i = 0; it.hasNext(); i++) {
            Element element = it.next();
            String tag = element.getQName();
            Attributes atts = element.getAttributes();

            if (tag.equals("setvar")) {
                if (atts.getIndex("action") != -1) {
                    responseProcessing.setAction(atts.getValue("action"));
                }

                DecimalFormat df = new DecimalFormat("#0.##");
                DecimalFormatSymbols decimalFormatSymbols = df.getDecimalFormatSymbols();
                decimalFormatSymbols.setDecimalSeparator('.');
                df.setDecimalFormatSymbols(decimalFormatSymbols);
                double value = 0.0;
                try {
                    value = df.parse(element.getValue().replace(',', '.')).doubleValue();
                } catch (ParseException e) {
                    throw new ParseQuestionException("Erro na cotação da pergunta", e);
                }
                responseProcessing.setResponseValue(value);
                if (responseProcessing.getAction().intValue() == ResponseProcessing.SET
                        || responseProcessing.getAction().intValue() == ResponseProcessing.ADD) {
                    if (subQuestion.getQuestionValue() == null || (subQuestion.getQuestionValue().compareTo(value) < 0)) {
                        subQuestion.setQuestionValue(value);
                    }
                } else if (responseProcessing.getAction().intValue() == ResponseProcessing.SUBTRACT) {
                    if (subQuestion.getQuestionValue() == null) {
                        subQuestion.setQuestionValue(new Double("-" + value));
                    }
                    responseProcessing.setResponseValue(new Double("-" + value));
                }
                for (ResponseProcessing rp : auxList) {
                    if (rp.getResponseProcessingId() == responseProcessing.getResponseProcessingId()) {
                        rp.setAction(atts.getValue("action"));
                        rp.setResponseValue(responseProcessing.getResponseValue());
                    }
                }
            } else if (tag.equals("respcondition")) {
                if (validResponseProcessing(responseProcessing)) {
                    auxList.add(responseProcessing);
                }
                responseProcessingId++;
                responseProcessing = new ResponseProcessing(responseProcessingId);
            } else if (tag.equals("other")) {
                responseProcessing.setOtherResponseProcessing(true);
            } else if (tag.equals("unanswered")) {
                responseProcessing.setUnansweredResponseProcessing(true);
            } else if (tag.startsWith("var")) {
                if (tag.equals("varequal") || tag.equals("varlt") || tag.equals("varlte") || tag.equals("vargt") || tag.equals("vargte")
                        || tag.equals("varsubstring")) {
                    if (!atts.getValue("respident").equals(questionElement.getResponseId())) {
                        throw new ParseQuestionException("ExercÃ­cio InvÃ¡lido (identificadores invÃ¡lidos)");
                    }
                    if (or == 0 && and == 0) {
                        String tagName = tag;
                        if (not) {
                            tagName = "not".concat(tagName);
                        }
                        if (atts.getIndex("case") != -1) {
                            if (atts.getValue("case").equals("Nocase")) {
                                tagName = tagName.concat("ignorecase");
                            }
                        }
                        if (subQuestion.getQuestionType().getType().intValue() == QuestionType.LID
                                && subQuestion.getQuestionType().getCardinalityType().getType().intValue() == CardinalityType.SINGLE) {
                            if (getNumberOfVarEquals(responseProcessing.getResponseConditions()) > 0) {
                                throw new ParseQuestionException(
                                        "Uma das soluÃ§Ãµes indicadas no ficheiro tem mais do que uma resposta, e uma pergunta de escolha simples apenas admite uma resposta.");
                            }
                        }
                        if (subQuestion.getQuestionType().getType().intValue() == QuestionType.NUM) {
                            try {
                                Double.parseDouble(element.getValue());
                            } catch (NumberFormatException e) {
                                invalidFeedback = true;
                                // throw new ParseQuestionException(
                                // "Uma das soluÃ§Ãµes indicadas no ficheiro tem um formato inválido.");
                            }
                        }
                        responseProcessing.getResponseConditions()
                                .add(new ResponseCondition(tagName, element.getValue(), atts.getValue("respident")));
                    }
                } else {
                    throw new ParseQuestionException(tag, true);
                }
            } else if (tag.equals("not") || tag.equals("/not")) {
                if (not) {
                    not = false;
                } else {
                    not = true;
                }
            } else if (tag.equals("and")) {
                if (and == 0 && or == 0) {
                    auxList.addAll(resolveAndCondition(questionElement, newResponseList, i, new ArrayList<ResponseProcessing>(), responseProcessingId));
                }
                and++;
            } else if (tag.equals("/and")) {
                and--;
            } else if (tag.equals("or")) {
                if (or == 0 && and == 0) {
                    auxList.addAll(resolveOrCondition(questionElement, newResponseList, i, new ArrayList<ResponseProcessing>(), responseProcessingId));
                }
                or++;
            } else if (tag.equals("/or")) {
                or--;
            } else if (tag.equals("displayfeedback")) {
                List<LabelValueBean> f = new ArrayList<LabelValueBean>();
                f.add(new LabelValueBean("linkrefid", atts.getValue("linkrefid")));
                responseProcessing.setFeedback(f);
                Iterator itAuxList = auxList.iterator();
                while (itAuxList.hasNext()) {
                    ResponseProcessing rp = (ResponseProcessing) itAuxList.next();
                    if (rp.getResponseProcessingId() == responseProcessing.getResponseProcessingId()) {
                        rp.setFeedback(f);
                    }
                }
            } else if (tag.equals("respcond_extension")) {
                boolean setNextItem = false;
                for (ResponseProcessing rp : auxList) {
                    if (rp.getResponseProcessingId() == responseProcessing.getResponseProcessingId()) {
                        rp.setNextItem(atts.getValue("itemident"));
                        setNextItem = true;
                    }
                }
                if (!setNextItem) {
                    responseProcessing.setNextItem(atts.getValue("itemident"));
                }
            }
        }
        if (validResponseProcessing(responseProcessing)) {
            auxList.add(responseProcessing);
        }
        subQuestion.setResponseProcessingInstructions(auxList);
        return subQuestion;
    }
    
    public boolean validResponseProcessing(ResponseProcessing rp) {
        return rp != null && (rp.getResponseConditions().size() != 0 || rp.isOtherResponseProcessing() || rp.isUnansweredResponseProcessing());
    }

    private SubQuestion getFeedback(QuestionElement questionElement, SubQuestion subQuestion) throws ParseQuestionException {
        List<LabelValueBean> feedbackAuxList = new ArrayList<LabelValueBean>();
        List<ResponseProcessing> responses = subQuestion.getResponseProcessingInstructions();
        String ident = "";
        for (Element element : questionElement.getListFeedback()) {
            String tag = element.getQName();
            Attributes atts = element.getAttributes();

            if (tag.equals("itemfeedback")) {
                ident = atts.getValue("ident");
            } else if (tag.startsWith("mat") && !tag.equals("material")) {
                if ((tag.equals("mattext"))) {
                    String textType = "text/plain";
                    if (atts.getIndex("texttype") != -1) {
                        textType = atts.getValue("texttype");
                    }
                    feedbackAuxList.add(new LabelValueBean(textType, element.getValue()));
                } else if ((tag.equals("matimage"))) {
                    if (atts.getIndex("label") != -1) {
                        feedbackAuxList.add(new LabelValueBean("image_label", atts.getValue("label")));
                    }
                    if (atts.getIndex("uri") != -1) {
                        throw new ParseQuestionException(tag, "uri");
                    }
                    feedbackAuxList.add(new LabelValueBean(atts.getValue("imagtype"), element.getValue()));
                } else {
                    throw new ParseQuestionException(tag, true);
                }
            } else if ((tag.startsWith("flow"))) {
                feedbackAuxList.add(new LabelValueBean("flow", ""));
            } else if (tag.equals("/itemfeedback")) {

                Iterator responsesIt = subQuestion.getResponseProcessingInstructions().iterator();
                for (int i = 0; responsesIt.hasNext(); i++) {
                    ResponseProcessing rp = (ResponseProcessing) responsesIt.next();
                    if (rp.getFeedback() != null && rp.getFeedback().size() > 0 && (rp.getFeedback().iterator().next()).getValue().equals(ident)) {
                        rp.setFeedback(feedbackAuxList);
                    }
                    responses.set(i, rp);
                }
                feedbackAuxList = new ArrayList<LabelValueBean>();
            }
        }
        subQuestion.setResponseProcessingInstructions(responses);
        return subQuestion;
    }

    // private String imageById(QuestionElement questionElement, int imageId) {
    // int imageIdAux = 1;
    // for (Element element : questionElement.getListQuestion()) {
    // String tag = element.getQName();
    // if ((tag.equals("matimage"))) {
    // if (imageIdAux == imageId)
    // return element.getValue();
    // imageIdAux++;
    // }
    // }
    // for (Element element : questionElement.getListOptions()) {
    // String tag = element.getQName();
    // if ((tag.equals("matimage"))) {
    // if (imageIdAux == imageId)
    // return element.getValue();
    // imageIdAux++;
    // }
    // }
    // for (Element element : questionElement.getListFeedback()) {
    // String tag = element.getQName();
    // if ((tag.equals("matimage"))) {
    // if (imageIdAux == imageId)
    // return element.getValue();
    // imageIdAux++;
    // }
    // }
    // return null;
    // }

    private String[] shuffleOptions(QuestionElement questionElement) {
        Vector<String> v = new Vector<String>();
        Vector<String> vRandom = new Vector<String>();
        int optionNumber = 0;
        for (Element element : questionElement.getListOptions()) {
            String tag = element.getQName();
            Attributes atts = element.getAttributes();

            if (tag.equals("response_label") || tag.equals("response_na")) {
                optionNumber++;
                if (atts.getValue(atts.getIndex("rshuffle")).equals("Yes")) {
                    v.add("");
                    vRandom.add(Integer.toString(v.size()));
                    continue;
                }
                v.add(Integer.valueOf(v.size() + 1).toString());
            }
        }

        Random r = new Random();
        boolean ready = false;
        for (String id : vRandom) {
            while (!ready) {
                int index = (r.nextInt(1000) % optionNumber);
                if (v.elementAt(index).equals("")) {
                    v.removeElementAt(index);
                    ready = true;
                    v.insertElementAt(id, index);
                } else {
                    ready = false;
                }
            }
            ready = false;
        }
        return v.toArray(new String[v.size()]);
    }

    private List<QuestionOption> shuffleStudentTestQuestionOptions(String[] shuffle, List<QuestionOption> oldList) {
        if (shuffle == null) {
            return oldList;
        }
        List<QuestionOption> newList = new ArrayList<QuestionOption>();
        // String[] aux = shuffle.substring(1, shuffle.length() - 1).split(",
        // ");
        for (int i = 0; i < shuffle.length; i++) {
            newList.add(i, oldList.get(Integer.valueOf(shuffle[i].trim()).intValue() - 1));
        }

        return newList;
    }

    private List<ResponseProcessing> shuffleStudentTestQuestionResponses(String[] shuffle, List<ResponseProcessing> oldResponseProcessingList) {
        if (shuffle == null) {
            return oldResponseProcessingList;
        }

        List<ResponseProcessing> newResponseProcessingList = new ArrayList<ResponseProcessing>();
        for (ResponseProcessing oldResponseProcessing : oldResponseProcessingList) {
            List<ResponseCondition> newResponseConditionList = new ArrayList<ResponseCondition>();
            for (ResponseCondition oldResponseCondition : oldResponseProcessing.getResponseConditions()) {
                ResponseCondition newResponseCondition = oldResponseCondition;
                newResponseCondition.setResponse(Integer.valueOf(getPosition(shuffle, oldResponseCondition.getResponse())).toString());
                newResponseConditionList.add(newResponseCondition);
            }
            ResponseProcessing newReponseProcessing = oldResponseProcessing;
            newReponseProcessing.setResponseConditions(newResponseConditionList);
            newResponseProcessingList.add(newReponseProcessing);
        }

        return newResponseProcessingList;
    }

    private int getPosition(String[] shuffle, String value) {
        for (int i = 0; i < shuffle.length; i++) {
            if (shuffle[i].equals(value)) {
                return i + 1;
            }
        }
        return 0;
    }

    public void newResponseList(List<ResponseProcessing> responseList, List<QuestionOption> optionList) {
        for (ResponseProcessing responseProcessing : responseList) {
            List<ResponseCondition> newResponseConditionList = new ArrayList<ResponseCondition>();
            for (ResponseCondition responseCondition : responseProcessing.getResponseConditions()) {
                String response = responseCondition.getResponse();
                ResponseCondition newResponseCondition = null;
                int index = 1;
                for (QuestionOption option : optionList) {
                    if (option.getOptionId().equals(response)) {
                        newResponseCondition =
                                new ResponseCondition(ResponseCondition.getConditionString(responseCondition.getCondition()), Integer.valueOf(index)
                                        .toString(), responseCondition.getResponseLabelId());
                    } else {
                        index++;
                    }
                }
                newResponseConditionList.add(newResponseCondition);
            }
            responseProcessing.setResponseConditions(newResponseConditionList);
        }
        return;
    }

    public SubQuestion setFenixCorrectResponse(SubQuestion subQuestion) {
        if (subQuestion.getResponseProcessingInstructions().size() != 0) {
            Iterator itResponseProcessing = subQuestion.getResponseProcessingInstructions().iterator();
            int fenixCorrectResponseIndex = -1;
            double maxValue = 0;
            int previewsAction = 0;
            for (int i = 0; itResponseProcessing.hasNext(); i++) {
                ResponseProcessing responseProcessing = (ResponseProcessing) itResponseProcessing.next();
                if (responseProcessing.getResponseValue() != null && responseProcessing.getAction() != null
                        && !responseProcessing.getResponseConditions().isEmpty()) {

                    if ((responseProcessing.getResponseValue().doubleValue() > maxValue)
                            || (responseProcessing.getResponseValue().doubleValue() == maxValue && previewsAction == 0)
                            || (responseProcessing.getResponseValue().doubleValue() == maxValue && previewsAction != ResponseProcessing.SET && responseProcessing
                                    .getAction().intValue() == ResponseProcessing.SET)) {
                        maxValue = responseProcessing.getResponseValue().doubleValue();
                        fenixCorrectResponseIndex = i;
                        previewsAction = responseProcessing.getAction().intValue();
                    }
                }
            }
            if (fenixCorrectResponseIndex != -1) {
                (subQuestion.getResponseProcessingInstructions().get(fenixCorrectResponseIndex)).setFenixCorrectResponse(true);
            }
        }
        return subQuestion;
    }

    public SubQuestion removeRepeatedConditions(SubQuestion subQuestion) {
        List<ResponseProcessing> newRpList = new ArrayList<ResponseProcessing>();
        if (subQuestion.getResponseProcessingInstructions().size() > 1) {
            boolean isLID = false;
            if (subQuestion.getQuestionType().getType().intValue() == QuestionType.LID) {
                isLID = true;
            }

            newRpList.add(subQuestion.getResponseProcessingInstructions().iterator().next());
            for (ResponseProcessing responseProcessing : subQuestion.getResponseProcessingInstructions()) {
                if (!responseProcessing.isThisConditionListInResponseProcessingList(newRpList, isLID)) {
                    newRpList.add(responseProcessing);
                }
            }
            subQuestion.setResponseProcessingInstructions(newRpList);
        }
        return subQuestion;
    }

    private int getNumberOfVarEquals(List<ResponseCondition> rcList) {
        int result = 0;
        for (ResponseCondition responseCondition : rcList) {
            if (responseCondition.getCondition().intValue() == ResponseCondition.VAREQUAL) {
                result++;
            }
        }
        return result;
    }

    public SubQuestion getRidOfEmptyResponseConditions(SubQuestion subQuestion) {
        List<ResponseProcessing> newResponseProcessingInstructions = new ArrayList<ResponseProcessing>();
        for (ResponseProcessing rp : subQuestion.getResponseProcessingInstructions()) {
            boolean empty = true;
            if ((rp.getNextItem() != null && rp.getNextItem().length() != 0) || rp.isOtherResponseProcessing() || rp.isUnansweredResponseProcessing()) {
                empty = false;
            } else {
                for (ResponseCondition rc : rp.getResponseConditions()) {
                    if (rc.getCondition().intValue() != ResponseCondition.NOTVAREQUAL) {
                        empty = false;
                    }
                }
            }
            if (!empty) {
                newResponseProcessingInstructions.add(rp);
            }
        }
        subQuestion.setResponseProcessingInstructions(newResponseProcessingInstructions);
        return subQuestion;
    }

    private List<Element> getRidOfNot(QuestionElement questionElement) {
        List<Element> resultList = new ArrayList<Element>();
        int not = 0;
        for (Element element : questionElement.getListResponse()) {
            String tag = element.getQName();
            if (tag.startsWith("var")) {
                if ((not % 2) == 0) {
                    resultList.add(element);
                } else {
                    resultList.add(NOT_ELEMENT);
                    resultList.add(element);
                    resultList.add(SLASH_NOT_ELEMENT);
                }
            } else if (tag.startsWith("other") || tag.startsWith("unanswered")) {
                if ((not % 2) != 0) {
                    if (tag.startsWith("other")) {
                        element.setQName("unanswered");
                    } else {
                        element.setQName("other");
                    }
                }
                resultList.add(element);
            } else if (tag.equals("not")) {
                not++;
            } else if (tag.equals("/not")) {
                not--;
            } else if (tag.equals("and") || tag.equals("/and")) {
                if ((not % 2) == 0) {
                    resultList.add(element);
                } else {
                    String tagName = "or";
                    if (tag.equals("/and")) {
                        tagName = "/" + tagName;
                    }
                    resultList.add(new Element(null, tagName, tagName, null));
                }

            } else if (tag.equals("or") || tag.equals("/or")) {
                if ((not % 2) == 0) {
                    resultList.add(element);
                } else {
                    String tagName = "and";
                    if (tag.equals("/or")) {
                        tagName = "/" + tagName;
                    }
                    resultList.add(new Element(null, tagName, tagName, null));
                }
            } else {
                resultList.add(element);
            }
        }
        return resultList;
    }

    private List<ResponseProcessing> resolveAndCondition(QuestionElement questionElement, List<Element> listResponse, int index,
            List<ResponseProcessing> oldResponseList, int id) throws ParseQuestionException {
        ListIterator it = listResponse.listIterator(index);
        boolean not = false;
        List<ResponseProcessing> newResponseList = new ArrayList<ResponseProcessing>();
        int or = 0;
        for (int i = 0; it.hasNext(); i++) {
            Element element = (Element) it.next();
            String tag = element.getQName();
            Attributes atts = element.getAttributes();

            if (tag.startsWith("var") && or == 0) {
                if (tag.equals("varequal") || tag.equals("varlt") || tag.equals("varlte") || tag.equals("vargt") || tag.equals("vargte")
                        || tag.equals("varsubstring")) {
                    if (!atts.getValue("respident").equals(questionElement.getResponseId())) {
                        throw new ParseQuestionException("ExercÃ­cio InvÃ¡lido (identificadores invÃ¡lidos)");
                    }
                    String tagName = tag;
                    if (not) {
                        tagName = "not".concat(tagName);
                    }
                    if (atts.getIndex("case") != -1) {
                        if (atts.getValue("case").equals("Nocase")) {
                            tagName = tagName.concat("ignorecase");
                        }
                    }
                    ResponseCondition rc = new ResponseCondition(tagName, element.getValue(), atts.getValue("respident"));

                    if (newResponseList.size() != 0) {
                        Iterator newResponseListIt = newResponseList.iterator();
                        while (newResponseListIt.hasNext()) {
                            ResponseProcessing rp = (ResponseProcessing) newResponseListIt.next();
                            rp.getResponseConditions().add(rc);

                        }
                    } else {
                        ResponseProcessing rp = new ResponseProcessing(id);
                        List<ResponseCondition> rcList = new ArrayList<ResponseCondition>();
                        rcList.add(rc);
                        rp.setResponseConditions(rcList);
                        newResponseList.add(rp);
                    }

                }
            } else if (tag.equals("or")) {
                if (or == 0) {
                    newResponseList.addAll(resolveOrCondition(questionElement, listResponse, index + i, newResponseList, id));
                }
                or++;
            } else if ((tag.equals("not") || tag.equals("/not")) && or == 0) {
                if (not) {
                    not = false;
                } else {
                    not = true;
                }
            } else if (tag.equals("/and") && or == 0) {
                oldResponseList.addAll(newResponseList);
                return oldResponseList;
            } else if (tag.equals("/or")) {
                or--;
            }

        }
        oldResponseList.addAll(newResponseList);
        return oldResponseList;
    }

    private List<ResponseProcessing> resolveOrCondition(QuestionElement questionElement, List<Element> listResponse, int index,
            List<ResponseProcessing> oldResponseList, int id) throws ParseQuestionException {
        ListIterator it = listResponse.listIterator(index);
        boolean not = false;
        List<ResponseProcessing> newResponseList = new ArrayList<ResponseProcessing>();
        int and = 0;
        for (int i = 0; it.hasNext(); i++) {
            Element element = (Element) it.next();
            String tag = element.getQName();
            Attributes atts = element.getAttributes();

            if (tag.startsWith("var") && and == 0) {
                if (tag.equals("varequal") || tag.equals("varlt") || tag.equals("varlte") || tag.equals("vargt") || tag.equals("vargte")
                        || tag.equals("varsubstring")) {
                    if (!atts.getValue("respident").equals(questionElement.getResponseId())) {
                        throw new ParseQuestionException("ExercÃ­cio InvÃ¡lido (identificadores invÃ¡lidos)");
                    }
                    String tagName = tag;
                    if (not) {
                        tagName = "not".concat(tagName);
                    }
                    if (atts.getIndex("case") != -1) {
                        if (atts.getValue("case").equals("Nocase")) {
                            tagName = tagName.concat("ignorecase");
                        }
                    }
                    if (oldResponseList.size() != 0) {
                        Iterator oldResponseListIt = oldResponseList.iterator();
                        while (oldResponseListIt.hasNext()) {
                            ResponseProcessing responseProcessing = (ResponseProcessing) oldResponseListIt.next();
                            responseProcessing.getResponseConditions().add(
                                    new ResponseCondition(tagName, element.getValue(), atts.getValue("respident")));
                            newResponseList.add(responseProcessing);
                        }
                    } else {
                        ResponseProcessing responseProcessing = new ResponseProcessing(id);
                        responseProcessing.getResponseConditions()
                                .add(new ResponseCondition(tagName, element.getValue(), atts.getValue("respident")));
                        newResponseList.add(responseProcessing);
                    }
                }
            } else if (tag.equals("and")) {
                if (and == 0) {
                    newResponseList = resolveAndCondition(questionElement, listResponse, index + i, newResponseList, id);
                }
                and++;
            } else if ((tag.equals("not") || tag.equals("/not")) && and == 0) {
                if (not) {
                    not = false;
                } else {
                    not = true;
                }
            } else if (tag.equals("/or") && and == 0) {
                return newResponseList;
            } else if (tag.equals("/and")) {
                and--;
            }

        }
        return newResponseList;
    }

    private static final ConcurrentMap<Question, List<SubQuestion>> questionsMap = new ConcurrentHashMap<>();

    public static List<SubQuestion> getSubQuestionFor(final Question question) {
        return questionsMap.getOrDefault(question, Collections.emptyList());
    }

    private static void setSubQuestionFor(final Question question, final List<SubQuestion> subQuestions) {
        questionsMap.putIfAbsent(question, subQuestions);
    }

}