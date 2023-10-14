import React from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar/NavBar';
import { checkIsServiceProviderLoggedIn } from '../AuthContext';

const ServiceProviderHome = ({ component: Component, ...rest }) => {
  const access = checkIsServiceProviderLoggedIn();
  const navigate = useNavigate();

  if (!access) {
    navigate('/provider/login'); // Redirect to the login page or an error page
    return null; // Return null to prevent rendering anything on invalid access
  }

  return (
    <div>
      <Navbar />
    </div>
  );
};

export default ServiceProviderHome;