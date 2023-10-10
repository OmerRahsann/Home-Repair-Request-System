import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../AuthContext";

function SignUpForm() {
  const navigate = useNavigate()
  const {accessAcount} = useAuth();
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    type: "SERVICE_REQUESTER",
    accountInfo: {
      firstName:"",
      middleName:"",
      lastName:"",
      address:"",
      phoneNumber:""
  }

  });
  const passwordsMatch = () => formData.password === formData.confirmPassword;

  const handleConfirmPasswordChange = (e) => {
    const { value } = e.target;
    // Update the confirmPassword field
    setFormData((prevData) => ({
      ...prevData,
      confirmPassword: value
    }));
  };
 

  const handleChange = (e) => {
    const { name, value } = e.target;
  
    // For nested objects (accountInfo), you need to spread them correctly
    if (name.includes('accountInfo.')) {
      const accountInfo = { ...formData.accountInfo };
      const field = name.split('.')[1];
      accountInfo[field] = value;
  
      setFormData({
        ...formData,
        accountInfo: accountInfo
      });
    } else {
      setFormData({
        ...formData,
        [name]: value
      });
    }
  };
  

  async function save(event) {
    event.preventDefault();
    try {
      const { email, password, type, accountInfo } = formData;
      await axios.post("http://localhost:8080/api/register", {
        email: email,
        password: password,
        type: type,
        accountInfo: {
          firstName: accountInfo.firstName,
          middleName: accountInfo.middleName,
          lastName: accountInfo.lastName,
          address: accountInfo.address,
          phoneNumber: accountInfo.phoneNumber
        }
      });
      alert("user Registation Successfully");
      accessAcount()
      navigate("/")
    } catch (err) {
      alert(err);
    }
  }

  return (
    <div className="form-container sign-up-container">
      <form onSubmit={save}>
        <h1 style={{color: "#565656"}}>Create Account</h1>
        <div className="flex justify-between">
          <input
            type="text"
            name="accountInfo.firstName"
            value={formData.accountInfo.firstName}
            onChange={handleChange}
            placeholder="First Name"
            required
          />
          <div className="p-1"></div>
          <input
            type="text"
            name="accountInfo.lastName"
            value={formData.accountInfo.lastName}
            onChange={handleChange}
            placeholder="Last Name"
            required
          />
        </div>
        <input
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          placeholder="Email"
        />
        <input 
          type="text"
          name="accountInfo.address"
          value={formData.accountInfo.address}
          onChange={handleChange}
          placeholder="Address"
          required
        /> 
        <input 
          type="text"
          name="accountInfo.phoneNumber"
          value={formData.accountInfo.phoneNumber}
          onChange={handleChange}
          placeholder="Phone Number"
          required
        /> 
      
        <input
          type="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          placeholder="Password"
          pattern=".{8,}"
          required
          title="Password must be at least 8 characters long."
        />
     
        <input
          type="password"
          name="confirmPassword"
          value={formData.confirmPassword}
          onChange={handleConfirmPasswordChange}
          placeholder="Confirm Password"
          required
        />
        {formData.confirmPassword && (
         <span className={passwordsMatch() ? 'text-green-500' : 'text-red-500'}>
         {passwordsMatch() ? '✓ Passwords match' : '✗ Passwords do not match'}
       </span>
        )}
        <div className="p-1 bg-blue"></div>
        <button disabled={!passwordsMatch()}className={`${passwordsMatch() ? '' : 'cursor-not-allowed'}` }>
          Sign Up
        </button>
      </form>
    </div>
  );
}

export default SignUpForm;
