import { useState, useEffect } from "react";
import ServiceRequest from "../../components/Customer/ServiceRequest";
import Navbar from "../../components/Navbar/NavBar";
import axios from "axios";
import { checkIsCustomerLoggedIn } from "../../AuthContext";

function MyRequests() {

  const [serviceRequests, setServiceRequests] = useState([])
  const [loggedIn, setLoggedIn] = useState(false); // Initialize loggedIn with a default value


  const getServiceRequests = async () => {
  
    try {
        const response = await axios.get(('http://localhost:8080/api/customer/service_request'), {withCredentials: true});
        setServiceRequests(response.data);
    } catch (error) {
        console.error('Error fetching service requests:', error);
    }
};

  useEffect(() => {
    const fetchData = async () => {
      try {
        const isLoggedIn = await checkIsCustomerLoggedIn();
        setLoggedIn(isLoggedIn); // Set the loggedIn state based on the result of the Promise
        if(loggedIn){
            return <h1>You are not logged in. Please sign in to make a request and get on the Radar!</h1>
        }
      } catch (error) {
        console.error('Error checking if customer is logged in:', error);
      }
    };

    fetchData(); // Call the function when the component mounts
    getServiceRequests()
    console.log(serviceRequests)

    // Define the checkIsCustomerLoggedIn function here or import it from where it's defined
  }, []);
    return (
      <div >
        <div>
          <Navbar isLoggedIn={loggedIn}/>
        </div>
        {serviceRequests.length != 0 ? (
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
      
    </div>) : <h1 className="font-bold text-center text-xl">You do not have any requests. Click here to get started:</h1>}
      </div> 
      
    );
  }
  export default MyRequests;