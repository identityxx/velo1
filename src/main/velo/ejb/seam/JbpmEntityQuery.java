package velo.ejb.seam;

import java.util.List;
import java.util.Map;

import javax.persistence.NonUniqueResultException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.persistence.QueryParser;

/**
 * A Query object for JBPM.
 * 
 */
public class JbpmEntityQuery<E> extends org.jboss.seam.framework.Query<Session, E>
{

   private List<E> resultList;
   private E singleResult;
   private Long resultCount;
   private Map<String, String> hints;

   /**
    * Validate the query
    * 
    * @throws IllegalStateException if the query is not valid
    */
   @Override
   public void validate()
   {
      super.validate();
      
      
      if ( getSession()==null )
      {
         throw new IllegalStateException("entityManager is null");
      }
   }

   @Override
   @Transactional
   public boolean isNextExists()
   {
      return resultList!=null && getMaxResults()!=null &&
             resultList.size() > getMaxResults();
   }


   /**
    * Get the list of results this query returns
    * 
    * Any changed restriction values will be applied
    */
   @Transactional
   @Override
   public List<E> getResultList()
   {
      if ( isAnyParameterDirty() )
      {
         refresh();
      }
      initResultList();
      return truncResultList(resultList);
   }

   private void initResultList()
   {
      if (resultList==null)
      {
         Query query = createQuery();
         resultList = query==null ? null : query.list();
      }
   }
   
   /**
    * Get a single result from the query
    * 
    * Any changed restriction values will be applied
    * 
    * @throws NonUniqueResultException if there is more than one result
    */
   @Transactional
   @Override
   public E getSingleResult()
   {
      if (isAnyParameterDirty())
      {
         refresh();
      }
      initSingleResult();
      return singleResult;
   }

   private void initSingleResult()
   {
      if ( singleResult==null)
      {
         Query query = createQuery();
         singleResult = (E) (query==null ? 
               null : query.uniqueResult());
      }
   }

   /**
    * Get the number of results this query returns
    * 
    * Any changed restriction values will be applied
    */
   @Transactional
   @Override
   public Long getResultCount()
   {
      if (isAnyParameterDirty())
      {
         refresh();
      }
      initResultCount();
      return resultCount;
   }

   private void initResultCount()
   {
      if ( resultCount==null )
      {
         Query query = createCountQuery();
         resultCount = query==null ? 
               null : (Long) query.uniqueResult();
      }
   }

   /**
    * The refresh method will cause the result to be cleared.  The next access
    * to the result set will cause the query to be executed.
    * 
    * This method <b>does not</b> cause the ejbql or restrictions to reread.
    * If you want to update the ejbql or restrictions you must call 
    * {@link #setEjbql(String)} or {@link #setRestrictions(List)}
    */
   @Override
   public void refresh()
   {
      super.refresh();
      resultCount = null;
      resultList = null;
      singleResult = null;
   }
   
   public Session getSession()
   {
      return getPersistenceContext();
   }

   public void setSession(Session session)
   {
      setPersistenceContext(session);
   }

   @Override
   protected String getPersistenceContextName()
   {
      return "jbpmHibernateSessionFactory";
   }
   
   protected Query createQuery()
   {
      parseEjbql();
      
      evaluateAllParameters();
      
      joinTransaction();
      
      Query query = getSession().createQuery( getRenderedEjbql() );
      setParameters( query, getQueryParameterValues(), 0 );
      setParameters( query, getRestrictionParameterValues(), getQueryParameterValues().size() );
      if ( getFirstResult()!=null) query.setFirstResult( getFirstResult() );
      if ( getMaxResults()!=null) query.setMaxResults( getMaxResults()+1 ); //add one, so we can tell if there is another page
      if ( getHints()!=null )
      {
         for ( Map.Entry<String, String> me: getHints().entrySet() )
         {
            //TODO: query.setHint(me.getKey(), me.getValue());
         }
      }
      return query;
   }
   
   protected Query createCountQuery()
   {
      parseEjbql();

      evaluateAllParameters();

      joinTransaction();
      
      Query query = getSession().createQuery( getCountEjbql() );
      setParameters( query, getQueryParameterValues(), 0 );
      setParameters( query, getRestrictionParameterValues(), getQueryParameterValues().size() );
      return query;
   }

   private void setParameters(Query query, List<Object> parameters, int start)
   {
      for (int i=0; i<parameters.size(); i++)
      {
         Object parameterValue = parameters.get(i);
         if ( isRestrictionParameterSet(parameterValue) )
         {
            query.setParameter( QueryParser.getParameterName(start + i), parameterValue );
         }
      }
   }

   public Map<String, String> getHints()
   {
      return hints;
   }

   public void setHints(Map<String, String> hints)
   {
      this.hints = hints;
   }
   
   protected void joinTransaction()
   {
	   /*
      try
      {
         Transaction.instance().enlist( getSession() );
      }
      catch (SystemException se)
      {
         throw new RuntimeException("could not join transaction", se);
      }
      */
   }
   
}