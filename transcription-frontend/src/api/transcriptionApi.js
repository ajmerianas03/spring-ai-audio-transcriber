import apiClient from "./apiClient";


export const uploadAudio = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const res = await apiClient.post("/transcribe", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });

  return res.data;
};


export const getHistory = async () => {
  const res = await apiClient.get("/transcribe/history");
  return res.data;
};
