import Navbar from "../components/Navbar/NavBar";

function Home() {
    return (
      <div >
        <div>
          <Navbar/>
        </div>
        <div className="text-center mt-8">
        <h1 className="text-3xl font-semibold">Welcome to My Website</h1>
        <p className="text-lg text-gray-600">This is some sample text on the screen.</p>
      </div>
      </div>
      
    );
  }
  export default Home;