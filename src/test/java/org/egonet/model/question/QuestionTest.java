package org.egonet.model.question;

import org.egonet.exceptions.MalformedQuestionException;
import org.egonet.model.Shared;
import org.egonet.model.answer.CategoricalAnswer;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import junit.framework.JUnit4TestAdapter;

public class QuestionTest {
	public static junit.framework.Test suite() {
	      return new JUnit4TestAdapter(QuestionTest.class);
	}
	
	@Test(expected=MalformedQuestionException.class)
	public void testBadQuestionClass() {
		Question.asSubclass("please-dont-exist-as-a-class");
	}
	
	@Test
	public void testQuestionTypesMatch() {
	    for(Class<? extends Question> clazz : Shared.questionClasses) {
	        
	        String clazzCanonicalName = clazz.getCanonicalName();
	        
	        assertTrue("Question.newInstance(" + clazzCanonicalName + ") should yield an instance of that type",
	        		Question.newInstance(clazz).getClass().equals(clazz));
	    }
	}
	
	@Test
	public void testQuestionTitles() {
	      assertTrue(new StudyQuestion().getTitle() == "Study questions");     
	      assertTrue(new EgoQuestion().getTitle() == "Questions About You");
	      assertTrue(new AlterQuestion().getTitle() == "<html><p>Questions About <nobr><b>$$1</b></nobr></p></html>");
	      assertTrue(new AlterPromptQuestion().getTitle() == "Whom do you know?");
	      assertTrue(new AlterPairQuestion().getTitle() == "<html><p>Questions About <nobr><b>$$1</b></nobr> and <nobr><b>$$2</b></nobr></p></html>");
	}
	
	@Test
	public void testGettersSetters() {
		StudyQuestion q = new StudyQuestion("ignore");
    	for(boolean t : new boolean[]{true,false}) {
    	  q.setFollowupOnly(t);
      	  assertTrue(q.isFollowupOnly() == t);
    	}
    	
    	for(boolean t : new boolean[]{true,false}) { 
    	  q.setStatable(t);
    	  assertTrue(q.isStatable() == t);
    	}
    	
    	CategoricalAnswer a = new CategoricalAnswer();
    	q.setAnswer(a);
    	assertTrue(q.getAnswer() == a);
    	
    	Selection sAdjacent = new Selection();
      	sAdjacent.setAdjacent(true);
      	sAdjacent.setValue(12345);
      
      	Selection [] selections = new Selection[]{
    	    new Selection(),
    	    sAdjacent, // determinesAdjacency=true
    	    new Selection()
    	};
    	q.setSelections(Arrays.asList(selections));
    	
    	Selection [] rSelections = q.getSelections().toArray(new Selection[0]);
    	for(int i = 0; i < selections.length; i++) {
    	  assertTrue(selections[i] == rSelections[i]);
    	}
    	
    	assert(q.determinesAdjacency());
    	assert(q.selectionAdjacent(12345));
	}
}
