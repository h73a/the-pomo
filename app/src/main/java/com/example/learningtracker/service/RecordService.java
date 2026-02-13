package com.example.learningtracker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.example.learningtracker.entity.LearningSubject;
import com.example.learningtracker.entity.Record;
import com.example.learningtracker.form.RecordForm;
import com.example.learningtracker.repository.LearningSubjectRepository;
import com.example.learningtracker.repository.RecordRepository;
import com.example.learningtracker.config.LoginUserDetails;

@Service
public class RecordService {

	@Autowired
	private RecordRepository recordRepository;

    @Autowired
    private LearningSubjectRepository learningSubjectRepository;
		
	public List<Record> findAllRecords(){
        return recordRepository.findAll();
	}

    public List<Record> findAllIsPublished() {
        return recordRepository.findByIsPublished(true);
    }

	public List<Record> findRecordsByLSubject(LearningSubject lSubject){
        return recordRepository.findByLearningSubjectIdOrderByIdAsc(lSubject.getId());
	}

    public List<Record> findAllRecordsByUser(LoginUserDetails loginUser) {
        Integer userId = loginUser.getUser().getId();
        List<Record> userRecords = recordRepository.findByLearningSubject_User_IdOrderByIdDesc(userId);
        return userRecords;
    }

    public List<Record> findAllRecordsByUserRecent(LoginUserDetails loginUser) {
        Integer userId = loginUser.getUser().getId();
        List<Record> userRecords = recordRepository.findByLearningSubject_User_IdOrderByCreatedAtDesc(userId);
        return userRecords;
    }

    public List<Record> findAllRecordsByDate(LoginUserDetails loginUser, LocalDate date) {
        Integer userId = loginUser.getUser().getId();
        List<Record> dateRecords = recordRepository.findByDateAndLearningSubject_User_Id(date, userId);
        return dateRecords;
    }

    public Integer dairyCount(List<Record> recordList) {
        Integer count = 0;
        if(recordList.size() > 0) {
            for(Record record : recordList) {
                count += 1;
            }
        }
        return count;
    }

    public LocalTime totalSumTime(List<Record> recordList) {
        LocalTime total = LocalTime.of(0, 0);
        if(recordList.size() > 0) {
            for(Record record : recordList) {
                long time = record.getSumTime().getLong(ChronoField.MINUTE_OF_DAY);
                total = total.plusMinutes(time);
            }
        }
        return total;
    }

    public List<LearningSubject> lSubjectList(List<Record> recordList) {
        List<LearningSubject> lSubjectList = new ArrayList<>();
        if(recordList.size() > 0) {
            for(Record record : recordList) {
                lSubjectList.add(record.getLearningSubject());
            }
            lSubjectList = new ArrayList<LearningSubject>(new LinkedHashSet<>(lSubjectList));
        }
        return lSubjectList;
    }

    public Integer totalPomodoro(List<Record> recordList) {
        Integer pomodoro = 0;
        if(recordList.size() > 0) {
            for(Record record : recordList) {
                if (record.getPomodoro() != null) {
                    pomodoro += record.getPomodoro();
                }
            }
        }
        return pomodoro;
    }

    public void update(RecordForm form, @AuthenticationPrincipal LoginUserDetails loginUser) {
        LearningSubject learningSubject = learningSubjectRepository.findById(form.getLearningSubjectId())
        .orElseThrow(() -> new RuntimeException("LearningSubject not found"));
		
		if (loginUser.getUser().getId().equals(learningSubject.getUserId())) {
            Record record = new Record();
            record.setId(form.getId());
            record.setLearningSubjectId(form.getLearningSubjectId());
            record.setDate(form.getDate());
            record.setStartTime(form.getStartTime());
            record.setStopTime(form.getStopTime());
            record.setBreakTime(form.getBreakTime());
            record.setSumTime(form.getSumTime());
            record.setPomodoro(form.getPomodoro());
            record.setUsesPomodoro(form.getUsesPomodoro());
            record.setMemo(form.getMemo());
            record.setIsPublished(form.getIsPublished());
            record.setUpdatedAt(LocalDateTime.now());
            
            recordRepository.save(record);
        } 
    }

}
