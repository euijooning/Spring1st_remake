package com.example.spring1st_re.service;

import com.example.spring1st_re.dto.BoardResponse;
import com.example.spring1st_re.dto.CreateBoardRequest;
import com.example.spring1st_re.dto.DeleteBoardRequest;
import com.example.spring1st_re.dto.UpdateBoardRequest;
import com.example.spring1st_re.entity.Board;
import com.example.spring1st_re.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service // 이게 없으면 서버가 안 뜬다.
public class BoardService {
    private final BoardRepository boardRepository;
    // Repository를 상속받은 이 인터페이스는 DB처럼 동작한다. DB에게 시킬 때 얘한테 시키면 된다.

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    } // ??? 이거 질문하자. => 이것도@RequiredArgsConstructor 쓰면 되나?

    // 게시글 전체 조회 -> 요 코드 해석 부탁드립니당... 튜터님께서 쓰셔서 쓰긴 했는데, 무슨 의미인지 모르겠어요. 어레이리스트부터 for문의 의미...
    @Transactional
    public List<BoardResponse> getBoardList() {
        // 작성 날짜 기준 내림차순으로 정리하기 ==> 게시물 작성 최신순으로 정렬해 내놓으란 뜻.
        List<Board> boardList = boardRepository.findAllByOrderByCreatedAtDesc();
        List<BoardResponse> boardResponseList = new ArrayList<>();
        for (Board board : boardList) {
            boardResponseList.add(new BoardResponse((board)));
        }
        return boardResponseList;
    }

    @Transactional
    // 게시글 생성 로직
    public void createBoard(CreateBoardRequest createBoardRequest) {
        Board board = new Board(createBoardRequest.getTitle(), createBoardRequest.getWriter(), createBoardRequest.getPassword(), createBoardRequest.getContent());
        boardRepository.save(board);
    }

    @Transactional
    // 게시글 조회 로직
    public BoardResponse getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("id 없음"));
        return new BoardResponse(board);
    }

    // 게시글 수정 로직
    @Transactional
    public void updateBoard(Long boardId, UpdateBoardRequest updateBoardRequest) {
        Board boardSaved = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("id 없음"));
        //    boardSaved.update(updateBoardRequest.getTitle(), updateBoardRequest.getWriter(), updateBoardRequest.getContent());
        //  추가요건 : 수정요청 시 수정 데이터와 비밀번호를 함께 보내서 서버에서 비밀번호 일치 여부 확인 후 업데이트 해라.
        // 이게 앞서 넣어놓은(Board 에다가) 비밀번호 유효성 검사.
        if (boardSaved.isValidPassword(updateBoardRequest.getPassword())) {
            boardSaved.update(updateBoardRequest.getTitle(), updateBoardRequest.getWriter(), updateBoardRequest.getContent());
            boardRepository.save(boardSaved);
        } else {
            throw new IllegalArgumentException("패스워드가 틀렸습니다!");
        }
    }

    // 게시글 삭제 로직
    @Transactional
    public void deleteBoard(Long boardId, DeleteBoardRequest deleteBoardRequest) {
        Board boardDelete = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("id 없음"));
        String password = deleteBoardRequest.getPassword();
        if (boardDelete.isValidPassword(password)) {
            boardRepository.delete(boardDelete); //delete는 JPA에서 직접 제공 => 쿼리 직접 날려준다
            System.out.println("삭제에 성공했습니다.");
        } else {
            throw new IllegalArgumentException("패스워드가 다릅니다!");
        }
    }

}


