import { useState, useEffect } from "react";
import ServiceRequest from "../components/Customer/ServiceRequest";
import Navbar from "../components/Navbar/NavBar";
import axios from "axios";

function Home() {

  const [serviceRequests, setServiceRequests] = useState([])

const getServiceRequests = async () => {
    try {
        const response = await axios.get('http://localhost:8080/api/customer/service_request');
        setServiceRequests(response.data);
    } catch (error) {
        console.error('Error fetching service requests:', error);
    }
};

    return (
      <div >
        <div>
          <Navbar/>
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