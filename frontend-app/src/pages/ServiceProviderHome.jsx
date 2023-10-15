import React, {useState} from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar/NavBar';
import { checkIsServiceProviderLoggedIn } from '../AuthContext';
import SearchBar from '../components/ServiceProviderHome/SearchBar';
import ServiceRequestList from '../components/ServiceProviderHome/ServiceRequestList';

const ServiceProviderHome = ({ component: Component, ...rest }) => {
  const access = checkIsServiceProviderLoggedIn();
  const navigate = useNavigate();
  const [searchKeyword, setSearchKeyword] = useState('');

  if (!access) {
    navigate('/provider/login'); // Redirect to the login page or an error page
    return null; // Return null to prevent rendering anything on invalid access
  }

  return (
    <div>
      <Navbar />
      <div>
        <SearchBar onSearch={setSearchKeyword}/>
        <ServiceRequestList keyword={searchKeyword} />
      </div>
    </div>
  );
};

export default ServiceProviderHome;