/**
 * <p>Title: Egocentric Networks Client Program</p>
 * <p>Description: Subject Interview Client</p>
 * <p>Copyright: Copyright (c) 2002 - 2004 </p>
 * <p>Company: Endless Loop Software</p>
 * @author Peter Schoaff
 *
 * $Id: AlterPairSubmitAction.java,v 1.8 2004/04/05 21:23:57 admin Exp $
 */
package com.endlessloopsoftware.egonet.web.actions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jboss.logging.Logger;

import com.endlessloopsoftware.egonet.AlterPair;
import com.endlessloopsoftware.egonet.interfaces.InterviewEJBLocal;
import com.endlessloopsoftware.egonet.interfaces.InterviewSBLocal;
import com.endlessloopsoftware.egonet.interfaces.InterviewSBLocalHome;
import com.endlessloopsoftware.egonet.interfaces.InterviewSBUtil;
import com.endlessloopsoftware.egonet.util.AnswerDataValue;
import com.endlessloopsoftware.egonet.util.InterviewDataValue;
import com.endlessloopsoftware.egonet.web.WebShared;
import com.endlessloopsoftware.egonet.web.forms.QuestionListForm;

/**
 * @author admin
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class AlterPairSubmitAction extends ELSAction
{
	InterviewSBLocal _interviewSB;
	
	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#perform(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward perform(	ActionMapping mapping, ActionForm pform, 
									HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		Logger 					logger 			= Logger.getLogger(this.getClass());
		QuestionListForm 		form 				= (QuestionListForm) pform;
		int[][]				 	matrix;
		InterviewEJBLocal 	interview;
		InterviewDataValue 	interviewData;
		AnswerDataValue[]		previousAnswers;
		
		logger.debug("AlterQuestionSubmitAction::perform");
				
		// Lookup Interview
		try 
		{
			// Get Interview from session context
			interview         = WebShared.retrieveInterview(request);
			interviewData     = WebShared.retrieveInterviewDataValue(request);
			previousAnswers   = interviewData.getAnswerDataValues();
			matrix            = interviewData.getAdjacencyMatrix();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		Vector answers = form.getAnswers();
		for (Iterator it = answers.iterator(); it.hasNext();)
		{
			AnswerDataValue 	answer 	= (AnswerDataValue) it.next();
			boolean 				found		= false;

			for (int i = 0; i < previousAnswers.length; ++i)
			{
				if (previousAnswers[i].equals(answer))
				{
					previousAnswers[i].setAnswerString(answer.getAnswerString());
					previousAnswers[i].fillAnswer();
					interviewData.updateAnswerDataValue(previousAnswers[i]);
					
					answer = previousAnswers[i];
					found = true;
				}
			}
			
			if (!found)
			{
				answer.fillAnswer();
				interviewData.addAnswerDataValue(answer);
			}
			
			// Fill Adjacency Matrix
			/**
			 * @TODO handle multiple alter pair questions
			 * This, in the end, will just store the value from the last one
			 */
			AlterPair alters = answer.getAlters();		
			logger.debug("Filling Matrix: " + alters.getPrimaryAlter() + " <=> " + alters.getSecondaryAlter() + " :: " + (answer.getAnswerAdjacent() ? "adjacent" : "--------"));
			matrix[alters.getPrimaryAlter()][alters.getSecondaryAlter()] = answer.getAnswerAdjacent() ? 1 : 0;
			matrix[alters.getSecondaryAlter()][alters.getPrimaryAlter()] = answer.getAnswerAdjacent() ? 1 : 0;

			//logger.debug(answer.getAnswerString());
		}
		
		try
		{
			{
				int adjacencies = 0;
				for (int i = 0; i < matrix.length; ++i)
					for (int j = 0; j < i; ++j)
						if (matrix[i][j] == 1)
							++adjacencies;
				logger.debug("Storing matrix with " + adjacencies + " adjacencies");
			}
					
			interviewData.setAdjacencyMatrix(matrix);
			interview.setInterviewDataValue(interviewData);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		return (mapping.findForward(WebShared.FORWARD_SUCCESS));
	}
	
	public InterviewSBLocal getInterviewSB()
	{
		if (_interviewSB == null)
		{	
			try
			{
				InterviewSBLocalHome interviewSBHome = InterviewSBUtil.getLocalHome();
				_interviewSB = interviewSBHome.create();
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return _interviewSB;
	}
	
}


/**
 * $Log: AlterPairSubmitAction.java,v $
 * Revision 1.8  2004/04/05 21:23:57  admin
 * Can't cache InterviewDataValue since matrix is set from another session.
 * Applet Linking now works.
 *
 * Revision 1.7  2004/04/05 01:16:43  admin
 * Modifying to use new Applet Linking Interface
 *
 * Revision 1.6  2004/03/18 15:23:40  admin
 * Recovers previous interview state
 *
 * Revision 1.5  2004/03/12 18:05:28  admin
 * Applet now works under Windows IE.
 * Fixed layout issues related to struts-layout converting spaces to nbsp
 * Using Servlet for applet/server communications
 *
 * Revision 1.4  2004/02/15 14:59:01  admin
 * Fixing Header Tags
 *
 * Revision 1.3  2004/02/15 14:44:15  admin
 * fixing headers
 *
 * Revision 1.2  2004/02/15 14:41:56  admin
 * Answer Data Value taking a couple of new parameters to aid display of
 * Alter Names
 *
 * Revision 1.1  2004/02/15 14:37:38  admin
 * Displaying network graph on web pages
 *
 */