import { useState } from "react";

const ImageUpload = ({ onChange = () => {} }) => {

  const [thumbnail, setThumbnail] = useState(null);
  const [main, setMain] = useState([]);
  const [details, setDetails] = useState([]);

  const thumbnailHandler = (e) => {
    const file = e.target.files[0];

    setThumbnail(file);

    onChange({
      thumbnail: file,
      main,
      details,
    });
  };

  const mainHandler = (e) => {
    const files = Array.from(e.target.files);

    setMain(files);

    onChange({
      thumbnail,
      main: files,
      details,
    });
  };

  const detailHandler = (e) => {
    const files = Array.from(e.target.files);

    setDetails(files);

    onChange({
      thumbnail,
      main,
      details: files,
    });
  };

  return (
    <div className="image-upload">

      <div>
        <label>썸네일</label>
        <input
          type="file"
          accept="image/*"
          onChange={thumbnailHandler}
        />
      </div>

      <div>
        <label >메인 이미지</label>
        <input
          type="file"
          multiple
          accept="image/*"
          onChange={mainHandler}
        />
      </div>

      <div>
        <label >상세 이미지</label>
        <input
          type="file"
          multiple
          accept="image/*"
          onChange={detailHandler}
        />
      </div>

    </div>
  );
};

export default ImageUpload;