/*
 * Generated by XDoclet - Do not edit!
 */
package com.endlessloopsoftware.egonet.interfaces;

/**
 * Local interface for QuestionEJB.
 */
public interface QuestionEJBLocal
   extends javax.ejb.EJBLocalObject
{

   public java.lang.Long getId(  ) ;

   public int getQuestionType(  ) ;

   public int getAnswerType(  ) ;

   public java.lang.String getTitle(  ) ;

   public java.lang.String getText(  ) ;

   public void addAnswer( com.endlessloopsoftware.egonet.util.AnswerDataValue added ) throws javax.ejb.FinderException;

   public void removeAnswer( com.endlessloopsoftware.egonet.util.AnswerDataValue removed ) throws javax.ejb.RemoveException;

   public void addSelection( com.endlessloopsoftware.egonet.util.SelectionDataValue added ) throws javax.ejb.CreateException;

   public void removeSelection( com.endlessloopsoftware.egonet.util.SelectionDataValue removed ) throws javax.ejb.RemoveException;

   public void updateSelection( com.endlessloopsoftware.egonet.util.SelectionDataValue updated ) throws javax.ejb.FinderException;

   public com.endlessloopsoftware.egonet.util.QuestionDataValue getQuestionDataValue(  ) ;

   public void setQuestionDataValue( com.endlessloopsoftware.egonet.util.QuestionDataValue valueHolder ) ;

   public java.util.Set getSelections(  ) ;

   public void setSelections( java.util.Set selections ) ;

   public java.util.Set getAnswers(  ) ;

   public void setAnswers( java.util.Set answers ) ;

   public com.endlessloopsoftware.egonet.interfaces.StudyEJBLocal getStudy(  ) ;

   public void setStudy( com.endlessloopsoftware.egonet.interfaces.StudyEJBLocal study ) ;

   public com.endlessloopsoftware.egonet.interfaces.QuestionLinkEJBLocal getQuestionLink(  ) ;

   public void setQuestionLink( com.endlessloopsoftware.egonet.interfaces.QuestionLinkEJBLocal questionLink ) ;

}