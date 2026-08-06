import axios from "axios";
import { API_SERVER_URL } from "../commonApi";

export const getProductList = (productType, page = 0, size = 12) => {
  const params = {
    page,
    size,
  };

  if (productType) {
    params.productType = productType;
  }

  return axios.get(`${API_SERVER_URL}/api/product`, {
    params,
  });
};

export const getAdminProductList = (page = 0, size = 10) => {
  return axios.get(`${API_SERVER_URL}/api/product`, {
    params: {
      page,
      size,
    },
  });
};

export const insertProduct = (formData) => {
  return axios.post(`${API_SERVER_URL}/api/product`, formData);
};

export const getProductDetail = (productId) => {
  return axios.get(`${API_SERVER_URL}/api/product/${productId}`);
};

export const deleteProduct = (productId) =>
  axios.delete(`${API_SERVER_URL}/api/product/${productId}`);

export const updateProduct = (productId, data) => {
  return axios.put(`${API_SERVER_URL}/api/product/${productId}`, data, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

export const deleteImage = (fileId) => {
  return axios.delete(`${API_SERVER_URL}/api/product/image/${fileId}`);
};

export const deleteAllImages = (productId) => {
  return axios.delete(`${API_SERVER_URL}/api/product/${productId}/images`);
};
