import React from "react";
import "../../../css/shop/cart/ConfirmModal.css";

const ConfirmModal = ({ message, onConfirm, onCancel }) => {
  return (
    <div className="modal-overlay">
      <div className="confirm-modal">
        <p>{message}</p>
        <div className="modal-buttons">
          <button className="cancel-btn" onClick={onCancel}>
            취소
          </button>

          <button className="confirm-btn" onClick={onConfirm}>
            삭제
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmModal;
