# 프로젝트 데이터베이스 스키마

이 문서는 'Plomi' 프로젝트의 주요 데이터베이스 스키마와 각 테이블이 어떤 의도로 설계되었는지를 설명하는 문서입니다.

---

## 1. Users 테이블 (사용자 정보)

#### 설명: 이 테이블은 서비스에 가입한 사용자들의 기본 정보를 저장합니다. 사용자 인증과 식별의 핵심이 되는 테이블입니다.

> **필드**:
> * `id`: 각 사용자를 구분하는 고유 ID (PK, 자동 증가)
> * `username`: 로그인 아이디 (중복 불가, 필수 입력)
> * `password`: 암호화된 비밀번호 (필수 입력)
> * `email`: 이메일 (중복 불가, 선택 입력)
> * `name`: 실제 사용자 성명 (필수 입력)
> * `profile_image_url`: 프로필 사진 URL (선택 입력)
> * `status_message`: 상태 메시지 (선택 입력)
> * `role`: 사용자 권한 (`USER`, `ADMIN` 중 하나, 기본값 `USER`로 자동 지정)
> * `created_at`: 사용자 계정 생성 시각 (자동 기록)
> * `updated_at`: 마지막 정보 수정 시각 (자동 업데이트)

* `username`, `email`은 `UNIQUE` 제약을 걸어, 동일한 정보로 중복 가입하는 것을 방지합니다.
* 가입 회원의 핵심 정보를 집중적으로 관리하고, 정확한 사용자 식별이 가능한 필수 정보들로 구성하여 설계했습니다.

---

## 2. Diaries 테이블 (다이어리 글 정보)

#### 설명: 이 테이블은 회원이 작성한 다이어리 글의 정보를 저장합니다.

> **필드**:
> * `id`: 각 다이어리 글을 구별하는 고유 ID (PK, 자동 증가)
> * `title`: 다이어리 제목 (필수 입력)
> * `content`: 다이어리 본문 내용 (`TEXT` 타입으로 긴 내용 저장 가능, 필수 입력)
> * `date`: 다이어리가 작성된 날짜 (필수 입력)
> * `author_id`: 다이어리 작성자 ID (`Users` 테이블 참조, 작성자 탈퇴 시 `NULL` 처리 가능)
> * `is_private`: 다이어리의 공개/비공개 여부 (기본값 `FALSE`로 공개)
> * `diary_likes_count`: 해당 다이어리의 좋아요 수 (캐싱 필드, 기본값 0)
> * `view_count`: 해당 다이어리의 조회수(기본값 0)
> * `created_at`: 다이어리 최초 작성 시각
> * `updated_at`: 다이어리 마지막 수정 시각

* `CONSTRAINT unique_author_date UNIQUE (author_id, date)`: 한 유저가 같은 날짜에 하나의 다이어리만 작성할 수 있도록 제약합니다.
* `CONSTRAINT fk_diary_author FOREIGN KEY (author_id) REFERENCES Users(id) ON DELETE SET NULL`: 회원이 탈퇴할 경우, 해당 회원이 쓴
  다이어리는 작성자 정보만 `NULL`로 처리하여 게시물 자체는 삭제되지 않지만 "존재하지 않는 게시물입니다." 등으로 처리합니다.
* `diary_likes_count`는 `DiaryLikes` 테이블과 연동되어 변경이 생길때마다 애플리케이션에서 동기화합니다.
* `view_count `는 다이어리 글 조회시마다 애플리케이션 로직에 의해 증가합니다.

---

## 3. Schedules 테이블 (개인 일정 정보)

#### 설명: 이 테이블은 사용자의 개인 일정 정보를 저장합니다.

> * **필드**:
> * `id`: 각 일정을 구별하는 고유 ID (PK, 자동 증가)
> * `title`: 일정 제목 (필수 입력)
> * `description`: 일정에 대한 상세 설명 (`TEXT` 타입으로 긴 내용 저장 가능, 선택 입력)
> * `start_time`: 일정 시작 시간 (필수 입력)
> * `end_time`: 일정 종료 시간 (필수 입력)
> * `owner_id`: 해당**일정의 소유자**를 나타내는 사용자 ID (`Users`테이블 참조, 소유자 탈퇴시 일정도 삭제)
> * `created_at`: 일정 생성 시각
> * `updated_at`: 일정 마지막 수정 시각

* `CONSTRAINT fk_schedule_owner FOREIGN KEY (owner_id) REFERENCES Users(id) ON DELETE CASCADE`: 일정의 소유자가 회원 탈퇴할 경우, 그
  소유자의 모든 일정 정보는 DB에서 삭제됩니다.
* 일정 데이터는 개인적인 정보이기 때문에 소유자가 없는 상태로 남아있을 필요가 없다고 판단하여 데이터를 깔끔하게 정리합니다.
* `owner_id`라는 명칭을 사용함으로써 해당 `user_id`가 일정의 주된 소유자임을 알 수 있도록 의도를 명확히 했습니다.

---

## 4. Comments 테이블 (댓글 및 대댓글 정보)

#### 설명: 이 테이블은 다이어리 글에 달리는 댓글과 대댓글 정보를 저장합니다. 사용자 간의 소통과 자연스러운 대화 맥락 유지를 위한 테이블입니다.

> * **필드**:
> * `id`: 각 댓글을 구별하는 고유 ID (PK, 자동 증가)
> * `content`: 댓글 내용 (필수 입력)
> * `author_id`: 댓글을 작성한 사용자 ID (`Users` 테이블 참조, 작성자 탈퇴 시 `NULL`처리 가능)
> * `diary_id`: 해당 댓글이 달린 다이어리 글 ID (`Diaries` 테이블 참조, 다이어리 삭제 시 댓글도 삭제)
> * `parent_comment_id`: 대댓글 기능을 위한 부모 댓글의 ID (`Comments` 테이블 자체 참조, 부모 댓글 삭제 시 `NULL`처리 가능)
> * `is_deleted`: **댓글의 논리적(소프트) 삭제 여부 플래그** (기본값 `FALSE`로 삭제 안 됨)
> * `comment_likes_count`: 해당 댓글의 좋아요 수 (캐싱 필드, 기본값 0)
> * `created_at`: 댓글 최초 작성 시각
> * `updated_at`: 댓글 마지막 수정 시각

* `CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES Users(id) ON DELETE SET NULL`: 기존 댓글의 맥락은 유지하며 개인정보는
  보호하기 위해 댓글 작성자가 탈퇴해도 해당 댓글 자체는 보존하고 `author_id`만 `NULL`로 변경합니다.
* `CONSTRAINT fk_comment_diary FOREIGN KEY (diary_id) REFERENCES Diaries(id) ON DELETE CASCADE`: 다이어리가 삭제될 경우, 해당 게시물에
  달린 모든 댓글 및 대댓글은 DB에서 물리적으로 삭제합니다.
* `CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES Comments(id) ON DELETE SET NULL`: 부모 댓글을 **
  소프트 삭제(`is_deleted=TRUE`)**해도 대댓글은 부모 댓글과의 관계를 유지합니다. 부모 댓글이 '삭제된 댓글입니다.' 처리되어도 그 아래 대댓글들을 계속 보여줄 수 있으며,
  `ON DELETE SET NULL`은 부모 댓글이 **물리적으로 삭제되는 예외적인 경우**에 대비한 안전 장치입니다.
* 소프트 삭제 플래그(`is_deleted`)를 활용해 원본 내용을 보존하고 대댓글과의 연결을 유지하면서, 삭제된 댓글을 유연하게 표시할 수 있도록 합니다.
* 사용자가 댓글을 '삭제'하면 `is_deleted`를 `TRUE`로, `content`를 '삭제된 댓글입니다.' 등으로 **애플리케이션에서 업데이트**합니다.
* `comment_likes_count`는 `CommentLikes` 테이블과 연동되어 변경이 생길때마다 애플리케이션에서 동기화합니다.

---

## 5. DiaryLikes 테이블 (다이어리 좋아요 기록)

#### 설명: 이 테이블은 사용자가 다이어리 글에 '좋아요'를 누른 기록을 저장합니다.

> * **필드**:
> * `diary_id`: '좋아요'를 받은 다이어리 글의 ID (`Diaries` 테이블 참조, 다이어리 삭제 시 좋아요 기록도 삭제)
> * `user_id`: '좋아요'를 누른 사용자 ID (`Users` 테이블 참조, 사용자 탈퇴 시 좋아요 기록도 삭제)
> * `created_at`: '좋아요'를 누른 시각

* `PRIMARY KEY (diary_id, user_id)`: `diary_id`, `user_id`를 **복합 기본 키(Composite Primary Key)** 로 지정하여 한 사용자가 한 다이어리에
  여러번 '좋아요'를 누를 수 없도록 제한합니다. 별도의 고유 ID는 필요 없습니다.
* `CONSTRAINT fk_like_diary FOREIGN KEY (diary_id) REFERENCES Diaries(id) ON DELETE CASCADE`: 다이어리가 삭제될 경우, 해당 다이어리에 대한
  모든 '좋아요' 기록도 함께 삭제합니다.
* `CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE`: 사용자가 탈퇴할 경우, 해당 사용자의 모든 '좋아요'
  기록도 함께 삭제합니다.
* '좋아요'는 사용자의 특정 행위 기록이기 때문에, 행위의 주체나 대상이 사라질때 함께 사라지는 것이 데이터 일관성에 적합하다고 판단했습니다.
* `Diaries` 테이블의 `diary_likes_count` 필드는 이 `DiaryLikes` 테이블의 변경에 따라 동기화합니다.

---

## 6. CommentLikes 테이블 (댓글 좋아요 기록)

#### 설명: 이 테이블은 사용자가 댓글에 '좋아요'를 누른 기록을 관리합니다.

> * **필드**:
> * `comment_id`: '좋아요'를 받은 댓글의 ID (`Comments` 테이블 참조, 댓글 삭제 시 좋아요 기록도 삭제)
> * `user_id`: '좋아요'를 누른 사용자 ID (`Users`테이블 참조, 사용자 탈퇴 시 좋아요 기록도 삭제)
> * `created_at`: '좋아요'를 누른 시각

* `PRIMARY KEY (comment_id, user_id)`: `comment_id`, `user_id`를 **복합 기본 키(Composite Primary Key)** 로 지정하여 한 사용자가 한 댓글에
  여러번 '좋아요'를 누를 수 없도록 제한합니다. 별도의 고유 ID는 필요 없습니다.
* `CONSTRAINT fk_cl_comment FOREIGN KEY (comment_id) REFERENCES Comments(id) ON DELETE CASCADE`: 댓글이 **물리적으로 삭제**될 경우,
  해당 댓글에 대한 모든 '좋아요' 기록도 함께 삭제합니다.
* `CONSTRAINT fk_cl_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE`: 사용자가 탈퇴할 경우, 해당 사용자의 모든 '좋아요'
  기록도 함께 삭제됩니다.
* '댓글 좋아요' 역시 사용자의 특정 행위 기록이므로, 주체나 대상이 사라질 때 함께 사라지는 것이 데이터 일관성에 적합하다고 판단했습니다.
* 댓글이 **소프트 삭제(`is_deleted=TRUE`)** 된 경우에는 좋아요 기록은 물리적으로 남아있으며 물리적 삭제 일 경우에만 함께 제거됩니다.
* `Comments` 테이블의 `comment_likes_count` 필드는 이 `CommentLikes` 테이블의 변경에 따라 동기화합니다.