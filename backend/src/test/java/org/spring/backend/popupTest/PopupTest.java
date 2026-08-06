package org.spring.backend.popupTest;

import org.junit.jupiter.api.Test;
import org.spring.backend.admin.popup.dto.PopupDto;
import org.spring.backend.admin.popup.service.PopupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;

@SpringBootTest
public class PopupTest {
    @Autowired
    PopupService popupService;

    @Test
    void insert() throws IOException{
        for(int i=1; i<5; i++){
            PopupDto popupDto = PopupDto.builder()
                    .title("팝업 테스트" + i)
                    .content("테스트 내용" + i)
                    .linkUrl("/community/detail/" + i)
                    .active(true)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(10))
                    .sortOrder(i)
                    .build();

            try{
            popupService.insertPopup(popupDto);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }

        }
    }
}
