package com.swe.assignment.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;

import com.swe.assignment.bean.StudentBean;
import com.swe.assignment.dao.StudentRecord;

import io.kcache.Cache;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;

public class StudentKafkaImpl {
	// variable to hold the singleton database instance
	private static StudentKafkaImpl instance = null;
	public static final String TOPIC_NAME = "topic-3";
	public static final String SERVER = "localhost:9092";
	private Cache<String, StudentRecord[]> cache;

	private StudentKafkaImpl() {
		setCache();
	}

	/**
	 * creating a singleton instance of database connection class
	 * 
	 * @return
	 */
	public static synchronized StudentKafkaImpl getInstance() {
		// if singleton instance is not available create an instance object
		if (null == instance) {
			instance = new StudentKafkaImpl();
		}
		// else return the existing instance object
		return instance;
	}

	private void setCache() {
		Properties props = new Properties();
		props.put(KafkaCacheConfig.KAFKACACHE_BOOTSTRAP_SERVERS_CONFIG, SERVER);
		props.put(KafkaCacheConfig.KAFKACACHE_TOPIC_NUM_PARTITIONS_CONFIG, 3);
		props.put(KafkaCacheConfig.KAFKACACHE_TOPIC_REPLICATION_FACTOR_CONFIG, 1);
		props.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, TOPIC_NAME);
		props.put(KafkaCacheConfig.KAFKACACHE_TOPIC_REQUIRE_COMPACT_CONFIG, true);

		cache = new KafkaCache<>(new KafkaCacheConfig(props), Serdes.String(),
				Serdes.serdeFrom(new StudentRecord(), new StudentRecord()));
		cache.init();
	}

	public StudentBean readStudent(int id) throws Exception {
		StudentRecord[] records = cache.get(TOPIC_NAME);
		System.out.println("Fetched " + records + " records");
		for (StudentRecord record : records) {
			System.out.println("Received: " + record.getId());
			if (id == record.getId()) {
				return record.convert();
			}
		}
		return null;
	}

	public List<String> readStudentIds() throws Exception {
		List<String> studIDList = new ArrayList<String>();
		StudentRecord[] records = cache.get(TOPIC_NAME);
		System.out.println("Fetched " + records + " records");
		for (StudentRecord record : records) {
			System.out.println("Received: " + record.getId());
			studIDList.add(String.valueOf(record.getId()));
		}
		return studIDList;
	}

	public void saveToDatabase(StudentBean studentBean) throws Exception {
		StudentRecord stdRecord = new StudentRecord(studentBean);
		StudentRecord[] records = cache.get(TOPIC_NAME);
		StudentRecord[] nsurveys = new StudentRecord[1];
		if(null!=records) {
			nsurveys = new StudentRecord[records.length + 1];
			System.arraycopy(records, 0, nsurveys, 0, records.length);
			nsurveys[records.length] = stdRecord;
		}else {
			nsurveys[0] = stdRecord;
		}
		cache.put(TOPIC_NAME, nsurveys);
		System.out.println("Send record#" + stdRecord.getId());
	}

}
