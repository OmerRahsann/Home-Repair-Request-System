import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Authentication from "./pages/Authentication/Authentication";
import LocationFetcher from "./LocationFetcher";

function App() {
  return (
    <div
      <BrowserRouter>
        <LocationFetcher />
        <Routes>
          <Route path="/home" element={<Home />} />
          <Route path="/auth" element={<Authentication />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
