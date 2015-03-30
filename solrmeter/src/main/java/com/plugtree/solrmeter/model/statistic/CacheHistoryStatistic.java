/**
 * Copyright Plugtree LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.plugtree.solrmeter.model.statistic;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.google.inject.Inject;
import com.plugtree.solrmeter.model.QueryStatistic;
import com.plugtree.solrmeter.model.exception.QueryException;
import com.plugtree.solrmeter.model.exception.StatisticConnectionException;
import com.plugtree.stressTestScope.StressTestScope;

/**
 * 
 * Model of the Cache statistic. This statistic will show the evolution of the Solr caches
 * @author tflobbe
 *
 */
@StressTestScope
public class CacheHistoryStatistic implements QueryStatistic {

	/**
	 * Stores the historical data of the filterCache
	 */
	private SortedMap<Long, CacheData> filterCacheData;

    /**
     * Stores the historical data of the nCache
     */
    private SortedMap<Long, CacheData> nCacheData;	
	
	/**
     * Stores the historical data of the perSegFilter
     */
    private SortedMap<Long, CacheData> perSegFilterData;
	
	/**
	 * Stores the historical data of the documentCache
	 */
	private SortedMap<Long, CacheData> documentCacheData;
	
	/**
	 * Stores the historical data of the queryResultCache
	 */
	private SortedMap<Long, CacheData> queryResultCacheData;
	
	/**
	 * Stores the historical data of the fieldValueCache
	 */
	private SortedMap<Long, CacheData> fieldValueCacheData;

   /**
     * Stores the historical data of the fieldCache
     */
    private SortedMap<Long, CacheData> fieldCacheData;
	
	/**
	 * Stores the cumulative data of the filterCache
	 */
	private CacheData filterCacheCumulativeData;

    /**
     * Stores the cumulative data of the nCache
     */
    private CacheData nCacheCumulativeData;
	
    /**
     * Stores the cumulative data of the perSegmentFilter
     */
    private CacheData perSegFilterCumulativeData;	
	
	/**
	 * Stores the cumulative data of the documentCache
	 */
	private CacheData documentCacheCumulativeData;
	
	/**
	 * Stores the cumulative data of the queryResultCache
	 */
	private CacheData queryResultCacheCumulativeData;
	
	/**
	 * Stores the cumulative data of the fieldValueCache
	 */
	private CacheData fieldValueCacheCumulativeData;

    /**
     * Stores the cumulative data of the fieldCache
     */
    private CacheData fieldCacheCumulativeData;
    
	/**
	 * Stores the description of the filterCache
	 */
	private String filterCacheDescription;

    /**
     * Stores the description of the fieldValueCache
     */
    private String fieldValueCacheDescription;

    /**
     * Stores the description of the fieldCache
     */
    private String fieldCacheDescription;

    /**
     * Stores the description of the perSegFilter
     */
    private String perSegFilterDescription;

    /**
     * Stores the description of the nCache
     */
    private String nCacheDescription;

	/**
	 * Connection with Solr statistics
	 */
	private AbstractStatisticConnection connection;
	
	private long initTime;
	
	//TODO @configurable
	private long refreshInterval = 1000;
	
	private StatisticUpdateThread updateThread;
	
	@Inject
	public CacheHistoryStatistic(AbstractStatisticConnection connection) {
		super();
		this.connection = connection;
		this.filterCacheData = Collections.synchronizedSortedMap(new TreeMap<Long, CacheData>());
		this.perSegFilterData = Collections.synchronizedSortedMap(new TreeMap<Long, CacheData>());
		this.queryResultCacheData = Collections.synchronizedSortedMap(new TreeMap<Long, CacheData>());
		this.documentCacheData = Collections.synchronizedSortedMap(new TreeMap<Long, CacheData>());
		this.fieldValueCacheData = Collections.synchronizedSortedMap(new TreeMap<Long, CacheData>());
        this.fieldCacheData = Collections.synchronizedSortedMap(new TreeMap<Long, CacheData>());
		this.nCacheData = Collections.synchronizedSortedMap(new TreeMap<Long, CacheData>());
		this.initTime = System.currentTimeMillis();
	}
	
	
	public void updateData() {
		Long time = System.currentTimeMillis() - initTime;
		Map<String, CacheData> cacheData = null;
		try {
			cacheData = connection.getData();
			put(time, filterCacheData, cacheData, RequestHandlerConnection.FILTER_CACHE_NAME);
			put(time, perSegFilterData, cacheData, RequestHandlerConnection.PER_SEGMENT_FILTER_NAME);
			put(time, queryResultCacheData, cacheData, RequestHandlerConnection.QUERY_RESULT_CACHE_NAME);
			put(time, documentCacheData, cacheData, RequestHandlerConnection.DOCUMENT_CACHE_NAME);
			put(time, fieldValueCacheData, cacheData, RequestHandlerConnection.FIELD_VALUE_CACHE_NAME);
            put(time, fieldCacheData, cacheData, RequestHandlerConnection.FIELD_CACHE_NAME);
			put(time, nCacheData, cacheData, RequestHandlerConnection.NCACHE_NAME);
			
			filterCacheCumulativeData = cacheData.get(RequestHandlerConnection.CUMULATIVE_FILTER_CACHE_NAME);
            perSegFilterCumulativeData = cacheData.get(RequestHandlerConnection.CUMULATIVE_PER_SEGMENT_FILTER_NAME);
			queryResultCacheCumulativeData = cacheData.get(RequestHandlerConnection.CUMULATIVE_QUERY_RESULT_CACHE_NAME);
			documentCacheCumulativeData = cacheData.get(RequestHandlerConnection.CUMULATIVE_DOCUMENT_CACHE_NAME);
			fieldValueCacheCumulativeData = cacheData.get(RequestHandlerConnection.CUMULATIVE_FIELD_VALUE_CACHE_NAME);
            fieldCacheCumulativeData = cacheData.get(RequestHandlerConnection.CUMULATIVE_FIELD_CACHE_NAME);
			nCacheCumulativeData = cacheData.get(RequestHandlerConnection.CUMULATIVE_NCACHE_NAME);
			
		} catch (StatisticConnectionException e) {
			Logger.getLogger(this.getClass()).error("Could not update statistic", e);
		}
		
	}
	
	private void put(Long time, SortedMap<Long,CacheData> destDataMap, Map<String, CacheData> connectionData, String cacheName) {
	  CacheData cacheDataValue = connectionData.get(cacheName);
	  if(cacheDataValue != null) {
	    destDataMap.put(time, cacheDataValue);
	  }
  }


    public SortedMap<Long, CacheData> getFilterCacheData() {
		return filterCacheData;
	}

    public SortedMap<Long, CacheData> getPerSegFilterData() {
        return perSegFilterData;
    }

    public SortedMap<Long, CacheData> getNCacheData() {
        return nCacheData;
    }
    
	public SortedMap<Long, CacheData> getDocumentCacheData() {
		return documentCacheData;
	}


	public class StatisticUpdateThread extends Thread {
		
		private boolean running = false;
		
		public boolean isRunning() {
			return running;
		}

		@Override
		public synchronized void run() {
			while(running) {
				try {
					this.wait(Long.valueOf(refreshInterval));
					if(running) {
						updateData();
					}
				} catch (InterruptedException e) {
					Logger.getLogger(this.getClass()).error("Error on query thread", e);
					throw new RuntimeException(e);
				}
			}
		}
		
		@Override
		public synchronized void start() {
			this.running = true;
			super.start();
		}
		
		public synchronized void wake() {
			this.notify();
		}
		
		@Override
		public void destroy() {
			this.running = false;
		}

	}

	@Override
	public void onFinishedTest() {
		if(updateThread != null) {
			updateThread.destroy();
			updateThread = null;
		}
	}

	public SortedMap<Long, CacheData> getQueryResultCacheData() {
		return queryResultCacheData;
	}


	public SortedMap<Long, CacheData> getFieldValueCacheData() {
		return fieldValueCacheData;
	}

    public SortedMap<Long, CacheData> getFieldCacheData() {
        return fieldCacheData;
    }	

	public CacheData getFilterCacheCumulativeData() {
		return filterCacheCumulativeData;
	}

    public CacheData getPerSegFilterCumulativeData() {
        return perSegFilterCumulativeData;
    }
	
	public CacheData getDocumentCacheCumulativeData() {
		return documentCacheCumulativeData;
	}


	public CacheData getQueryResultCacheCumulativeData() {
		return queryResultCacheCumulativeData;
	}

	public CacheData getFieldValueCacheCumulativeData() {
		return fieldValueCacheCumulativeData;
	}

    public CacheData getFieldCacheCumulativeData() {
        return fieldCacheCumulativeData;
    }	
	
    public CacheData getNCacheCumulativeData() {
        return nCacheCumulativeData;
    }

	public String getFieldValueCacheDescription() {
		return fieldValueCacheDescription;
	}

	public String getFieldCacheDescription() {
		return fieldCacheDescription;
	}

	public String getFilterCacheDescription() {
		return filterCacheDescription;
	}

    public String getPerSegFilterDescription() {
        return perSegFilterDescription;
    }

    public String getNCacheDescription() {
        return nCacheDescription;
    }

	public long getRefreshInterval() {
		return refreshInterval;
	}


	@Override
	public void onExecutedQuery(QueryResponse response, long clientTime) {
		if(updateThread == null) {
			updateThread = new StatisticUpdateThread();
		}
		if(!updateThread.isRunning()) {
			updateThread.start();
		}
		
	}


	@Override
	public void onQueryError(QueryException exception) {}

}
