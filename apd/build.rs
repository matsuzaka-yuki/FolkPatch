use std::env;
use std::fs::File;
use std::io::{Read, Write};
use std::path::Path;
use std::process::Command;

fn get_gradle_version_code() -> Option<u32> {
    let gradle_file = "../build.gradle.kts";
    if let Ok(mut file) = File::open(gradle_file) {
        let mut content = String::new();
        if file.read_to_string(&mut content).is_ok() {
            // Look for "fun getVersionCode(): Int { return 113010 }"
            // We use a simple parsing approach
            if let Some(start) = content.find("fun getVersionCode(): Int {") {
                let rest = &content[start..];
                if let Some(return_idx) = rest.find("return") {
                    let rest = &rest[return_idx + 6..];
                    if let Some(end_idx) = rest.find('}') {
                        let num_str = rest[..end_idx].trim();
                        if let Ok(code) = num_str.parse::<u32>() {
                            return Some(code);
                        }
                    }
                }
            }
        }
    }
    None
}

fn get_git_version() -> Result<(u32, String), std::io::Error> {
    // Try to get version code from environment variable first
    let version_code: u32 = if let Ok(env_version_code) = env::var("APATCH_VERSION_CODE") {
        env_version_code.parse().unwrap_or(0)
    } else if let Some(gradle_code) = get_gradle_version_code() {
        gradle_code
    } else {
        // Fallback to git-based calculation
        let output = Command::new("git")
            .args(["rev-list", "--count", "HEAD"])
            .output()?;

        let output = output.stdout;
        let git_count = String::from_utf8(output).expect("Failed to read git count stdout");
        let git_count: u32 = git_count
            .trim()
            .parse()
            .map_err(|_| std::io::Error::new(std::io::ErrorKind::Other, "Failed to parse git count"))?;
        std::cmp::max(11000 + 200 + git_count, 10762) // For historical reasons and ensure minimum version
    };

    let version_name = String::from_utf8(
        Command::new("git")
            .args(["describe", "--tags", "--always"])
            .output()?
            .stdout,
    )
    .map_err(|_| {
        std::io::Error::new(
            std::io::ErrorKind::Other,
            "Failed to read git describe stdout",
        )
    })?;
    let mut version_name = version_name.trim_start_matches('v').to_string();

    if let Ok(env_version_name) = env::var("APATCH_VERSION_NAME") {
        version_name = env_version_name;
    } else {
        // Use the calculated/read version code in the name as well
        version_name = format!("{}-Matsuzaka-yuki", version_code);
    }

    Ok((version_code, version_name))
}

fn main() {
    // update VersionCode when git repository change
    println!("cargo:rerun-if-changed=../.git/HEAD");
    println!("cargo:rerun-if-changed=../.git/refs/");
    println!("cargo:rerun-if-changed=../build.gradle.kts");
    // Force rebuild every time
    println!("cargo:rerun-if-changed=build.rs");

    let (code, name) = match get_git_version() {
        Ok((code, name)) => (code, name),
        Err(_) => {
            // show warning if git is not installed
            println!("cargo:warning=Failed to get git version, using 0.0.0");
            (0, "0.0.0".to_string())
        }
    };
    let out_dir = env::var("OUT_DIR").expect("Failed to get $OUT_DIR");
    println!("out_dir: ${out_dir}");
    println!("code: ${code}");
    let out_dir = Path::new(&out_dir);
    File::create(Path::new(out_dir).join("VERSION_CODE"))
        .expect("Failed to create VERSION_CODE")
        .write_all(code.to_string().as_bytes())
        .expect("Failed to write VERSION_CODE");

    File::create(Path::new(out_dir).join("VERSION_NAME"))
        .expect("Failed to create VERSION_NAME")
        .write_all(name.trim().as_bytes())
        .expect("Failed to write VERSION_NAME");
}
