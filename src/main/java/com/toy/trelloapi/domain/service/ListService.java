package com.toy.trelloapi.domain.service;

import com.toy.trelloapi.domain.dto.CardDto;
import com.toy.trelloapi.domain.dto.WorkListDto;
import com.toy.trelloapi.domain.entity.Card;
import com.toy.trelloapi.domain.entity.WorkList;
import com.toy.trelloapi.domain.exception.WorkListNotFoundException;
import com.toy.trelloapi.domain.repository.WorkListQueryRepository;
import com.toy.trelloapi.domain.repository.WorkListRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListService {

    private final WorkListRepository workListRepository;
    private final WorkListQueryRepository workListQueryRepository;
    private final ModelMapper modelMapper;

    public WorkList saveWorkList(String workListTitle) {

        LocalDateTime currentDateTime = LocalDateTime.now();

       return workListRepository.save(
                WorkList.builder()
                        .workListTitle(workListTitle)
                        .workListOrd(getNextWorkListOrd())
                        .regId("admin")
                        .regDtime(currentDateTime)
                        .useYn(true)
                        .modId("admin")
                        .modDtime(currentDateTime)
                        .build()
        );
    }

    @Transactional
    public void updateWorkList(WorkListDto workListDto) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Optional<WorkList> optWorkList = workListRepository.findByWorkListId(workListDto.getWorkListId());
        WorkList workList = optWorkList.orElseThrow(() -> new WorkListNotFoundException("해당 리스트를 찾을 수 없습니다."));
        workList.changeWorkList(workListDto.getWorkListTitle(), "admin", currentDateTime);
    }

    private Long getNextWorkListOrd(){
        return workListQueryRepository.findLastWorkListOrd() + 1000L;
    }

    @Transactional
    public List<WorkListDto> findAll() {
        List<WorkList> workLists = workListRepository.findAllByUseYnEqualsOrderByWorkListOrd(true);

        return workLists.stream()
                .map(x -> {
                    List<Card> cardList = x.getCard();
                    List<CardDto> cardDtoList = cardList.stream()
                                                        .map(y -> CardDto.builder()
                                                                    .cardId(y.getCardId())
                                                                    .workListId(y.getWorkList().getWorkListId())
                                                                    .cardTitle(y.getCardTitle())
                                                                    .cardOrd(y.getCardOrd())
                                                                    .regId(y.getRegId())
                                                                    .regDTime(y.getRegDtime())
                                                                    .modId(y.getModId())
                                                                    .modDTime(y.getModDtime())
                                                                    .build()
                                                        )
                                                        .collect(Collectors.toList());
                    return WorkListDto.builder()
                                .workListId(x.getWorkListId())
                                .workListTitle(x.getWorkListTitle())
                                .workListOrd(x.getWorkListOrd())
                                .regId(x.getRegId())
                                .regDtime(x.getRegDtime())
                                .modId(x.getModId())
                                .modDtime(x.getModDtime())
                                .useYn(x.isUseYn())
                                .cardList(cardDtoList)
                                .build();
                })
                .collect(Collectors.toList());
    }
}
