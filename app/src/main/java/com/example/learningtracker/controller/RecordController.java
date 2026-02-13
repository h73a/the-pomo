package com.example.learningtracker.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.learningtracker.config.LoginUserDetails;
import com.example.learningtracker.entity.Record;
import com.example.learningtracker.entity.User;
import com.example.learningtracker.form.RecordForm;
import com.example.learningtracker.repository.RecordRepository;
import com.example.learningtracker.service.LearningSubjectService;
import com.example.learningtracker.service.RecordService;


@Controller
public class RecordController {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private RecordService recordService;

    @Autowired
    private LearningSubjectService learningSubjectService;

    @ModelAttribute
    public RecordForm recordForm() {
        return new RecordForm();
    }

    @GetMapping("/record")
    public String viewRecord(Model model, @AuthenticationPrincipal LoginUserDetails loginUser) {
        List<Record> recordList = recordService.findAllRecordsByUser(loginUser);
        model.addAttribute("userName", loginUser.getUser().getName());
        model.addAttribute("recordList", recordList);
        
        return "record/records";
    }

    @GetMapping("/record/create/form")
    public ModelAndView recordForm(@AuthenticationPrincipal LoginUserDetails loginUser, ModelAndView mv) {
        Record record = new Record();
        mv.setViewName("record/recordCreateForm");
        mv.addObject("lSubject", learningSubjectService.findAllByUserId(loginUser));
        mv.addObject("userName", loginUser.getUser().getName());
        mv.addObject("today", LocalDate.now());
        mv.addObject("record", record);

        return mv;
    }

    @PostMapping("/record/create/form")
    public ModelAndView newRecord(@AuthenticationPrincipal LoginUserDetails loginUser, ModelAndView mv) {
        Record record = new Record();
        mv.setViewName("record/recordCreateForm");
        mv.addObject("lSubject", learningSubjectService.findAllByUserId(loginUser));
        mv.addObject("userName", loginUser.getUser().getName());
        mv.addObject("today", LocalDate.now());
        mv.addObject("record", record);

        return mv;
    }

    @PostMapping("/record/create/do")
    public String createRecord(@AuthenticationPrincipal LoginUserDetails loginUser, @ModelAttribute @Validated RecordForm recordForm, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // バリデーションエラーの場合
        if (result.hasErrors()) {
            model.addAttribute("recordForm", recordForm);
            model.addAttribute("lSubject", learningSubjectService.findAllByUserId(loginUser));
            return "record/recordCreateForm";
        }

        try {
            recordService.update(recordForm, loginUser);
            redirectAttributes.addFlashAttribute("successMessage", "学習記録が正常に登録されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "学習記録の登録に失敗しました。");
        }
        return "redirect:/record";
    }

    @GetMapping("/record/id/{id}")
    public ModelAndView recordById(@PathVariable(name="id") int id, @AuthenticationPrincipal LoginUserDetails loginUser, ModelAndView mv) {
        Record record = recordRepository.findById(id).get();
        User user = loginUser.getUser();
        if (record.getLearningSubject().getUser().getId() .equals(user.getId())) {
            mv.addObject("record", record);
            mv.addObject("userName", user.getName());
            mv.setViewName("record/detail");
        } else {
            List<Record> recordList = recordService.findAllRecordsByUserRecent(loginUser);
            mv.addObject("recordList", recordList);
            mv.addObject("userName", user.getName());
            mv.setViewName("user/userHome");
        }

        return mv;
    }

    @GetMapping("/record/id/{id}/edit")
    public ModelAndView getEditRecord(@PathVariable(name="id") int id, @AuthenticationPrincipal LoginUserDetails loginUser, ModelAndView mv) {
        Record record = recordRepository.findById(id).get();
        User user = loginUser.getUser();

        if (record.getLearningSubject().getUser().getId().equals(user.getId())) {
            mv.addObject("record", record);
            mv.addObject("userName", user.getName());
            mv.addObject("lSubject", learningSubjectService.findAllByUserId(loginUser));
            mv.addObject("selectedValue", record.getLearningSubjectId());
            mv.setViewName("record/recordEditForm");
        } else {
            mv.setViewName("record/records");
        }
        return mv;
    }

    @PostMapping("/record/id/{id}/edit")
    public ModelAndView editRecord(@PathVariable(name="id") int id, @AuthenticationPrincipal LoginUserDetails loginUser, ModelAndView mv) {
        Record record = recordRepository.findById(id).get();
        User user = loginUser.getUser();
        if (record.getLearningSubject().getUser().getId().equals(user.getId())) {
            mv.addObject("record", record);
            mv.addObject("userName", user.getName());
            mv.addObject("lSubject", learningSubjectService.findAllByUserId(loginUser));
            mv.addObject("selectedValue", record.getLearningSubjectId());
            mv.setViewName("record/recordEditForm");
        } else {
            mv.setViewName("record/records");
        }
        return mv;
    }

    @PostMapping("/record/id/{id}/update")
    public String update(@AuthenticationPrincipal LoginUserDetails loginUser, @ModelAttribute @Validated RecordForm recordForm, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // バリデーションエラーの場合
        if (result.hasErrors()) {
            model.addAttribute("recordForm", recordForm);
            model.addAttribute("lSubject", learningSubjectService.findAllByUserId(loginUser));
            return "record/recordEditForm";
        }

        try {
            recordService.update(recordForm, loginUser);
            redirectAttributes.addFlashAttribute("successMessage", "学習記録が正常に更新されました。");
        } catch (EmptyResultDataAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定された学習記録が見つかりませんでした。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "学習記録の更新に失敗しました。");
        }

        return "redirect:/record";
    }

    @PostMapping("/record/id/{id}/delete")
    public String delete(@PathVariable(name="id") int id, @AuthenticationPrincipal LoginUserDetails loginUser, @ModelAttribute @Validated RecordForm recordForm, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Optional<Record> records = recordRepository.findById(id);
        Record record = records.get();

        // if (loginUser.getUser().getId().equals(record.getLearningSubject().getUser().getId())) {
            try {
                recordRepository.deleteById(record.getId());
                redirectAttributes.addFlashAttribute("successMessage", "学習記録が正常に削除されました。");
            } catch (EmptyResultDataAccessException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "指定された学習記録が見つかりませんでした。");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "学習記録の削除に失敗しました。");
            }
            return "redirect:/record";
        //}
        //    return "/record/id/{id}";
    }

    @GetMapping("/record/{date}")
    public String getDate(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, @AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
        List<Record> recordList = recordService.findAllRecordsByDate(loginUser, date);
        String title = date.toString() + "の学習記録 | The Pomo - 学習時間記録アプリ";
        model.addAttribute("title", title);
        model.addAttribute("date", date);
        model.addAttribute("count", recordService.dairyCount(recordList));
        model.addAttribute("totalSumTime", recordService.totalSumTime(recordList));
        model.addAttribute("lSubjectList", recordService.lSubjectList(recordList));
        model.addAttribute("totalPomodoro", recordService.totalPomodoro(recordList));
        return "record/dairy";
    }

    @GetMapping("/everyone")
    public String everyoneRecord(Model model, @AuthenticationPrincipal LoginUserDetails loginUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            List<Record> recordList = recordService.findAllIsPublished();
            if (recordList.size() > 0) {
            model.addAttribute("recordList", recordList);
            }
        }
        return "record/everyone";
    }
}