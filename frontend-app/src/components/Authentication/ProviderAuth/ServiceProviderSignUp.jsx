import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import ProviderDescription from "../../ProviderDescription";
import Select from 'react-select'



function SignUpForm() {
    const navigate = useNavigate()
    const services = [
        { value: "plumbing", label: "Plumbing" },
        { value: "yardwork", label: "Yardwork" },
        { value: "roofing", label: "Roofing" },
    ];
    const [selectedServices, setSelectedServices] = useState();
    const [description, setDescription] = useState('');

    // Callback function to set the description in the parent component's state
    const handleDescriptionChange = (newDescription) => {
        setDescription(newDescription);
    };
    const [formData, setFormData] = useState({
        email: "",
        password: "",
        type: "SERVICE_PROVIDER",
        accountInfo: {
            name: "",
            description: "",
            services: [],
            phoneNumber: "",
            address: "",
            contactEmailAddress: ""
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

    function handleSelect(data) {
        setSelectedServices(data);
    }

    async function save(event) {
        event.preventDefault();
        try {
            const { email, password, type, accountInfo } = formData;
            const selectedServiceValues = selectedServices.map((selectedService) => selectedService.value);
            await axios.post("http://localhost:8080/api/register", {
                email: email,
                password: password,
                type: type,
                accountInfo: {
                    name: accountInfo.name,
                    description: description,
                    services: selectedServiceValues,
                    phoneNumber: accountInfo.phoneNumber,
                    address: accountInfo.address,
                    contactEmailAddress: email
                }
            });
            alert("user Registation Successfully");
            navigate("/")
        } catch (err) {
            alert(err);
        }
    }

    return (
        <div className="form-container sign-up-container">
            <form onSubmit={save}>
                <h1 style={{ color: "#565656" }}>Create a Provider Account!</h1>

                <input
                    type="text"
                    name="accountInfo.name"
                    value={formData.accountInfo.name}
                    onChange={handleChange}
                    placeholder="Business Name"
                    required
                />

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
                    placeholder="Business Address"
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
                <div className="m-1"></div>
                <ProviderDescription onDescriptionChange={handleDescriptionChange}/>
                <div className="m-1"></div>
                <div className="m-1"> </div>
                <Select
                    options={services}
                    placeholder="What services are you offering?"
                    value={selectedServices}
                    onChange={handleSelect}
                    isSearchable={true}
                    isMulti
                    className="bg-custom-gray"
                />
                <div className="m-1"></div>
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
                <button disabled={!passwordsMatch()} className={`${passwordsMatch() ? '' : 'cursor-not-allowed'}`}>
                    Sign Up
                </button>
            </form>
        </div>
    );
}

export default SignUpForm;
