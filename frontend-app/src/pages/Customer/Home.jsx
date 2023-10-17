import { useState, useEffect } from "react";
import ServiceRequest from "../../components/Customer/ServiceRequest";
import Navbar from "../../components/Navbar/NavBar";
import axios from "axios";
import { checkIsCustomerLoggedIn } from "../../AuthContext";

function Home() {

  const [serviceRequests, setServiceRequests] = useState([])
  const [loggedIn, setLoggedIn] = useState(false); // Initialize loggedIn with a default value

  useEffect(() => {
    const fetchData = async () => {
      try {
        const isLoggedIn = await checkIsCustomerLoggedIn();
        setLoggedIn(isLoggedIn); // Set the loggedIn state based on the result of the Promise
      } catch (error) {
        console.error('Error checking if customer is logged in:', error);
      }
    };

    fetchData(); // Call the function when the component mounts
    getServiceRequests()

    // Define the checkIsCustomerLoggedIn function here or import it from where it's defined
  }, []);
 
  

const getServiceRequests = async () => {
  
    try {
        const response = await axios.get(('http://localhost:8080/api/customer/service_request'), {withCredentials: true});
        setServiceRequests(response.data);
    } catch (error) {
        console.error('Error fetching service requests:', error);
    }
};

    return (
      <div >
        <div>
          <Navbar isLoggedIn={loggedIn}/>
        </div>
        <div className="">
          <ServiceRequest/>
      </div>
      <div>
        <h1>hello</h1>
      </div>
      <div>
      <h1>Service Requests</h1>
      <ul>
  {serviceRequests.map((request) => (
    <li key={request.id}>
      <h2>{request.title}</h2>
      <p>{request.description}</p>
      <p>Budget: {request.dollars}</p>
    </li>
  ))}
</ul>
      
    </div>
      </div>
      
    );
  }
  export default Home;