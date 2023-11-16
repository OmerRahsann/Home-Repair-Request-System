import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./pages/Customer/Home";
import { AuthProvider } from "./AuthContext";
import CustomerSignIn from "./pages/Authentication/CustomerSignIn";
import CustomerSignUp from "./pages/Authentication/CustomerSignUp";
import ProviderSignUp from "./pages/Authentication/ProviderSignUp";
import ProviderSignIn from "./pages/Authentication/ProviderSignIn";
import RequestPasswordReset from "./pages/Authentication/ResetPassword/RequestPasswordReset";
import ResetPassword from "./pages/Authentication/ResetPassword/ResetPassword";
import RequestView from "./pages/Provider/RequestView"
import MyRequests from "./pages/Customer/MyRequests";
import MyJobs from "pages/Provider/MyJobs";
import { CustomerProfile } from "pages/Customer/CustomerProfile";
import { ProviderProfile } from "pages/Provider/ProviderProfile";
import Footer from "components/Footer";
import { MyQuotes } from "pages/Customer/MyQuotes";
import { Quotes } from "pages/Provider/Quotes";

function App() {
  return (
    <div>
    <div className="min-h-[92vh]">
      <AuthProvider>
        <Router>
          {/* <LocationFetcher /> */}
          <Routes>
            <Route path="/" element={<Home/>} />
            <Route path="/customer/login" element={<CustomerSignIn />} /> //thats prob why we are getting the 404 
            <Route path="/customer/signup" element={<CustomerSignUp/>} />
            <Route path="/customer/myrequests" element={<MyRequests/>}/>
            <Route path="/provider/signup" element={<ProviderSignUp/>} />
            <Route path="/provider/login" element={<ProviderSignIn/>} />
            <Route path="/provider/viewrequests" element={<RequestView/>} />
            <Route path="/reset_password" element={<RequestPasswordReset/>} />
            <Route path="/reset_password/form" element={<ResetPassword/>} />
            <Route path='/provider/myschedule' element={<MyJobs/>} />
            <Route path='/customer/myprofile' element={<CustomerProfile/>} />
            <Route path='/provider/myprofile' element={<ProviderProfile/>} />
            <Route path='/customer/myquotes' element={<MyQuotes/>} />
            <Route path='/provider/quotes' element={<Quotes/>} />
            
            
          </Routes>
        </Router>
      </AuthProvider>
      
    </div>
    <Footer/>
    </div>
  );
}

export default App;
