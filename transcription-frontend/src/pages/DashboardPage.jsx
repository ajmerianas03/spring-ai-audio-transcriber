
import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { BarChart, Clock, List } from "lucide-react";

const DashboardPage = () => {
  const royalBlue = "#002D62";
  const saffron = "#FF9933";

  const [stats, setStats] = useState([]); 
  const [loading, setLoading] = useState(true);

  // 🔹 Simulating API call (replace with real API later)
  useEffect(() => {
    setLoading(false);  
    setStats([]);       // empty → show message
  }, []);

  return (
    <div className="container py-5">
      <h2 className="mb-4 fw-bold" style={{ color: royalBlue }}>
        Welcome Back to $Bhaasha AI Dashboard
      </h2>
      <p className="lead text-muted mb-5">
        Your central hub for managing transcription and AI analysis tasks.
      </p>

      {/* Stats Summary Cards or Empty Message */}
      <div className="row g-4 mb-5">

        {loading ? (
          <p>Loading...</p>
        ) : stats.length === 0 ? (
          <div className="text-center py-5 text-muted">
            <h4>No dashboard data available yet.</h4>
            <p>Start uploading your audio files to generate insights.</p>
          </div>
        ) : (
          stats.map((stat, index) => (
            <div key={index} className="col-md-4">
              <div
                className="card shadow-sm h-100 border-0 rounded-3"
                style={{ borderBottom: `5px solid ${stat.color}` }}
              >
                <div className="card-body d-flex align-items-center">
                  <stat.icon size={36} className="me-3" style={{ color: stat.color }} />
                  <div>
                    <h6 className="card-subtitle mb-1 text-muted">{stat.title}</h6>
                    <h3 className="card-title fw-bold" style={{ color: stat.color }}>
                      {stat.value}
                    </h3>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}

      </div>

      {/* Call-to-Action Panel */}
      <div className="p-5 rounded-4 shadow-lg" style={{ backgroundColor: royalBlue }}>
        <h3 className="text-white mb-3">Quick Actions</h3>
        <p className="text-white-50 mb-4">Jump straight into your main tasks.</p>

        <div className="d-grid gap-3 d-sm-flex">
          <Link to="/upload" className="btn btn-warning btn-lg fw-bold shadow">
            <span className="me-2">🎙️</span> Start New Transcription
          </Link>
          <Link to="/history" className="btn btn-outline-light btn-lg fw-bold">
            <span className="me-2">📜</span> View Analysis History
          </Link>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;


// import React from 'react';
// import { Link } from 'react-router-dom';
// import { BarChart, Clock, List } from 'lucide-react'; // Placeholder icons for visualization

// const DashboardPage = () => {
//   const royalBlue = '#002D62';
//   const saffron = '#FF9933';
  
//   // Mock Data for Dashboard Summary
//   const stats = [
//     { title: "Total Transcripts", value: "145", icon: List, color: royalBlue },
//     { title: "Total Duration (Hrs)", value: "3.5", icon: Clock, color: saffron },
//     { title: "Avg. Analysis Score", value: "8.7/10", icon: BarChart, color: '#32CD32' }, // Lime Green for positive score
//   ];

//   return (
//     <div className="container py-5">
//       <h2 className="mb-4 fw-bold" style={{ color: royalBlue }}>
//         Welcome Back to $Bhaasha AI Dashboard
//       </h2>
//       <p className="lead text-muted mb-5">
//         Your central hub for managing transcription and AI analysis tasks.
//       </p>

//       {/* Stats Summary Cards */}
//       <div className="row g-4 mb-5">
//         {stats.map((stat, index) => (
//           <div key={index} className="col-md-4">
//             <div className="card shadow-sm h-100 border-0 rounded-3" style={{ borderBottom: `5px solid ${stat.color}` }}>
//               <div className="card-body d-flex align-items-center">
//                 <stat.icon size={36} className="me-3" style={{ color: stat.color }} />
//                 <div>
//                   <h6 className="card-subtitle mb-1 text-muted">{stat.title}</h6>
//                   <h3 className="card-title fw-bold" style={{ color: stat.color }}>{stat.value}</h3>
//                 </div>
//               </div>
//             </div>
//           </div>
//         ))}
//       </div>

//       {/* Call-to-Action Panel */}
//       <div className="p-5 rounded-4 shadow-lg" style={{ backgroundColor: royalBlue }}>
//         <h3 className="text-white mb-3">Quick Actions</h3>
//         <p className="text-white-50 mb-4">
//           Jump straight into your main tasks.
//         </p>
//         <div className="d-grid gap-3 d-sm-flex">
//           <Link to="/upload" className="btn btn-warning btn-lg fw-bold shadow">
//             <span className="me-2">🎙️</span> Start New Transcription
//           </Link>
//           <Link to="/history" className="btn btn-outline-light btn-lg fw-bold">
//             <span className="me-2">📜</span> View Analysis History
//           </Link>
//         </div>
//       </div>
//     </div>
//   );
// };

// export default DashboardPage;