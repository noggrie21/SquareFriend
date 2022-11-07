import React, { Dispatch, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import Modal from 'react-bootstrap/Modal';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import { EditNote } from '../../utils/Interface';
import { EditDefaultNote } from '../../utils/DefaultData';
import OMRApi from '../../api/OMRApi';
import { RootState } from '../../store/store';
import styles from './CreateMsg.module.scss';
import '../../style/style.scss';

interface Props {
  setPass: Dispatch<React.SetStateAction<boolean>>;
  pass: boolean;
  noteId: number;
}

function DetailMsg({ setPass, pass, noteId }: Props): JSX.Element {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const { omr, user } = useSelector((state: RootState) => state);
  const { codedEmail } = user;

  const [onEdit, setOnEdit] = useState<boolean>(false);
  const [editMsg, setEditMsg] = useState<EditNote>(EditDefaultNote);

  const readMsg = async () => {
    const response = await OMRApi.note.readNote(noteId);
    if (response.status === 200) {
      console.log(response.data);
      setEditMsg(response.data.data);
      console.log('디테일열림');
    } else {
      alert('메시지를 불러오지 못했습니다.');
    }
  };

  useEffect(() => {
    readMsg();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onEditClick = () => {
    setOnEdit(!onEdit);
  };

  const handleClose = () => setPass(false);

  const onDeleteClick = async () => {
    const del: boolean = window.confirm(
      '작성된 응원메시지를 삭제하시겠습니까?'
    );
    if (del) {
      try {
        const response = await OMRApi.note.deleteNote(noteId);
        if (response.status === 200) {
          alert('응원 메시지가 삭제되었습니다.');
          // dispatch로 새로운 omrList를 갱신해주는 코드가 필요할듯?
          navigate(`/cheer/${codedEmail}`);
        } else {
          alert('응원메시지를 삭제할 수 없습니다.');
        }
      } catch (err) {
        console.log(err);
      }
    }
    onEditClick();
  };

  // const onLikeClick = async (e: any) => {
  //   e.preventDefault();

  //   const response = await OMRApi.note.likeNote(noteId, favorite);
  // };

  return (
    <div>
      <Modal show={pass} onHide={handleClose} className={styles.test}>
        <Modal.Header closeButton>
          <Modal.Title>응원글 보기</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div style={{ display: 'flex', justifyContent: 'center' }}>
            <div style={{ width: '100%', padding: '0px' }}>
              <Row style={{ margin: '0px' }}>
                <div className={styles.group}>
                  <Col>
                    <Row>
                      <Col>
                        <label className={styles.form_label} htmlFor="nickname">
                          닉네임
                        </label>
                      </Col>
                      <Col>
                        <div>
                          <input
                            name="nickname"
                            id="nickname"
                            type="text"
                            value={editMsg.nickname}
                            maxLength={10}
                            disabled
                          />
                        </div>
                      </Col>
                    </Row>
                  </Col>
                  <Col>
                    <Row>
                      <Col>
                        <label className={styles.form_label} htmlFor="opendate">
                          공개 날짜
                        </label>
                      </Col>
                      <Col>
                        <div>
                          <input
                            name="showDate"
                            type="date"
                            id="opendate"
                            value={editMsg.showDate}
                            required
                          />
                        </div>
                      </Col>
                    </Row>
                  </Col>
                </div>
              </Row>
            </div>
          </div>
          <br />
          <div className={styles.cheer_box}>
            <div className={styles.cheerHeader}>
              <label className={styles.vertical_lr} htmlFor="cheer-text">
                서술형 응원
              </label>
              <div>
                <textarea
                  name="content"
                  placeholder="응원글을 작성해주세요."
                  id="cheer-text"
                  value={editMsg.content}
                  cols={30}
                  rows={10}
                  required
                />
                <ul>
                  <li>
                    <button
                      className={styles.btn_hover_border_3}
                      type="button"
                      onClick={onDeleteClick}
                    >
                      삭제
                    </button>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </Modal.Body>
      </Modal>
    </div>
  );
}

export default DetailMsg;
