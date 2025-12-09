import axios from "axios";

const AUTH_URL = "http://localhost:8080/api/auth";

export const loginUser = async (email, password) => {
  try {
    const res = await axios.post(`${AUTH_URL}/login`, { email, password });
    return res.data; 
  } catch (error) {
    throw error.response?.data || { message: "Login failed" };
  }
};

export const registerUser = async (userData) => {
  try {
    const res = await axios.post(`${AUTH_URL}/register`, userData);
    return res.data;
  } catch (error) {
    throw error.response?.data || { message: "Registration failed" };
  }
};
